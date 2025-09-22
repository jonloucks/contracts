package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.jonloucks.contracts.api.Checks.contractCheck;
import static io.github.jonloucks.contracts.api.Checks.promisorCheck;
import static java.util.Optional.ofNullable;

final class RepositoryImpl implements Repository, AutoClose {
    
    @Override
    public AutoClose open() {
        if (state.compareAndSet(IS_CLOSED, IS_OPEN)) {
            storedContracts.values().forEach(StorageImpl::bind);
            check();
            return this;
        }
        return ()->{};
    }
    
    @Override
    public void close() {
        if (state.compareAndSet(IS_OPEN, IS_CLOSED)) {
            storedContracts.values().forEach(StorageImpl::close);
            storedContracts.clear();
        }
    }
    
    @Override
    public <T> AutoClose store(Contract<T> contract, Promisor<T> promisor) {
        final Contract<T> validContract = contractCheck(contract);
        final Promisor<T> validPromisor = promisorCheck(promisor);
        
        if (storedContracts.containsKey(validContract)) {
            throw new ContractException( "The contract " + validContract + " already promised");
        }
        final StorageImpl<T> storage = new StorageImpl<>(service,validContract, validPromisor);
        
        storedContracts.put(validContract, storage);
  
        if (state.get()) {
            storage.bind();
        }
        return () -> {
            if (storedContracts.remove(validContract, storage)) {
                storage.close();
            }
        };
    }
    
    @Override
    public void check() {
        requiredContracts.forEach(contract -> {
            if (!service.isBound(contract)) {
                throw new ContractException( "The contract " + contract + " is required");
            }
        });
    }
    
    @Override
    public <T> void require(Contract<T> contract) {
        final Contract<T> validContract = contractCheck(contract);
        
        requiredContracts.add(validContract);
    }
    
    RepositoryImpl(Service service) {
        this.service = service;
    }
    
    private static final boolean IS_CLOSED = false;
    private static final boolean IS_OPEN = true;
    
    private final Service service;
    private final AtomicBoolean state = new AtomicBoolean(false);
    private final Set<Contract<?>> requiredContracts = new HashSet<>();
    
    /**
     * Using LinkedHashMap to retain insertion order
     */
    private final Map<Contract<?>, StorageImpl<?>> storedContracts = new LinkedHashMap<>();
    
    private static final class StorageImpl<T> implements AutoClose {
        private final Contract<T> contract;
        private final Promisor<T> promisor;
        private final Service service;
        private AutoClose closeBinding;
        
        StorageImpl(Service service, Contract<T> contract, Promisor<T> promisor) {
            this.service = service;
            this.contract = contract;
            this.promisor = promisor;
        }
    
        private void bind() {
            if (contract.isReplaceable() || !service.isBound(contract)) {
                close();
                closeBinding = service.bind(contract, promisor);
            }
        }
        
        @Override
        public void close() {
            if (ofNullable(closeBinding).isPresent() ){
                final AutoClose closeBinding = this.closeBinding;
                this.closeBinding = null;
                closeBinding.close();
            }
        }
    }
}
