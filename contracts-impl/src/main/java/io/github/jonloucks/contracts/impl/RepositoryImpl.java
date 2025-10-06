package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import java.util.*;

import static io.github.jonloucks.contracts.api.Checks.contractCheck;
import static io.github.jonloucks.contracts.api.Checks.promisorCheck;
import static java.util.Optional.ofNullable;

/**
 * Implementation for {@link io.github.jonloucks.contracts.api.Repository}
 * @see io.github.jonloucks.contracts.api.Repository
 */
final class RepositoryImpl implements Repository {
    
    @Override
    public AutoClose open() {
        if (openState.transitionToOpen()) {
            storedContracts.values().forEach(StorageImpl::bind);
            check();
            return this::close;
        }
        return ()->{};
    }
    
    @Override
    public <T> AutoClose store(Contract<T> contract, Promisor<T> promisor) {
        final Contract<T> validContract = contractCheck(contract);
        final Promisor<T> validPromisor = promisorCheck(promisor);
        
        if (storedContracts.containsKey(validContract)) {
            throw new ContractException( "The contract " + validContract + "  is already promised.");
        }
        final StorageImpl<T> storage = new StorageImpl<>(contracts,validContract, validPromisor);
        
        storedContracts.put(validContract, storage);
  
        if (openState.isOpen()) {
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
            if (!contracts.isBound(contract)) {
                throw new ContractException( "The contract " + contract + " is required.");
            }
        });
    }
    
    @Override
    public <T> void require(Contract<T> contract) {
        final Contract<T> validContract = contractCheck(contract);
        
        requiredContracts.add(validContract);
    }
    
    RepositoryImpl(Contracts contracts) {
        this.contracts = contracts;
    }
    
    private void close() {
        if (openState.transitionToClosed()) {
            storedContracts.values().forEach(StorageImpl::close);
            storedContracts.clear();
        }
    }
    
    /**
     * Using LinkedHashMap to retain insertion order
     */
    private final Map<Contract<?>, StorageImpl<?>> storedContracts = new LinkedHashMap<>();
    
    private static final class StorageImpl<T> implements AutoClose {

        StorageImpl(Contracts contracts, Contract<T> contract, Promisor<T> promisor) {
            this.contracts = contracts;
            this.contract = contract;
            this.promisor = promisor;
        }
    
        private void bind() {
            if (contract.isReplaceable() || !contracts.isBound(contract)) {
                close();
                closeBinding = contracts.bind(contract, promisor);
            }
        }
        
        @Override
        public void close() {
            ofNullable(closeBinding).ifPresent(close -> {
                this.closeBinding = null;
                close.close();
            });
        }
        
        private final Contract<T> contract;
        private final Promisor<T> promisor;
        private final Contracts contracts;
        private AutoClose closeBinding;
    }
    
    private final Contracts contracts;
    private final IdempotentImpl openState = new IdempotentImpl();
    private final Set<Contract<?>> requiredContracts = new HashSet<>();
}
