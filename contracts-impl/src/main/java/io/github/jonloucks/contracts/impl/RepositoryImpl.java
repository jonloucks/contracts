package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import java.util.*;

import static io.github.jonloucks.contracts.api.Checks.*;
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
    public <T> AutoClose store(Contract<T> contract, Promisor<T> promisor, BindStrategy bindStrategy) {
        final Contract<T> validContract = contractCheck(contract);
        final Promisor<T> validPromisor = promisorCheck(promisor);
        final BindStrategy validBindStrategy = nullCheck(bindStrategy, "bindStrategy");
        
        if (storedContracts.containsKey(validContract) && openState.isOpen()) {
            throw new ContractException( "The contract " + validContract + "  is already stored.");
        }
        
        final StorageImpl<T> storage = new StorageImpl<>(contracts, validContract, validPromisor, validBindStrategy);
        
        if (openState.isOpen()) {
            storage.bind();
        }
        
        storedContracts.put(validContract, storage);
  
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
            reverseCloseStorage();
        }
    }
    
    private void reverseCloseStorage() {
        final Stack<StorageImpl<?>> storageStack = new Stack<>();
        storedContracts.values().forEach(storageStack::push);
        try {
            while (!storageStack.isEmpty()) {
                storageStack.pop().close();
            }
        } finally {
            storedContracts.clear();
        }
    }
    
    /**
     * Using LinkedHashMap to retain insertion order
     */
    private final Map<Contract<?>, StorageImpl<?>> storedContracts = new LinkedHashMap<>();

    private static final class StorageImpl<T> implements AutoClose {

        StorageImpl(Contracts contracts, Contract<T> contract, Promisor<T> promisor, BindStrategy bindStrategy) {
            this.contracts = contracts;
            this.contract = contract;
            this.promisor = promisor;
            this.bindStrategy = bindStrategy;
        }
    
        private void bind() {
            close();
            closeBinding = contracts.bind(contract, promisor, bindStrategy);
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
        private final BindStrategy bindStrategy;
        private final Contracts contracts;
        private AutoClose closeBinding;
    }
    
    private final Contracts contracts;
    private final IdempotentImpl openState = new IdempotentImpl();
    private final Set<Contract<?>> requiredContracts = new HashSet<>();
}
