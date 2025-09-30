package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.*;
import static java.util.Optional.ofNullable;

final class ContractsImpl implements Contracts, AutoClose  {

    @Override
    public AutoClose open() {
        if (openState.transitionToOpen()) {
            closeRepository = respository.open();
            return this;
        }
        return ()->{};
    }
    
    @Override
    public void close() {
        if (openState.transitionToClosed()) {
            try {
                for (int attempts = 1, broken = breakAllBindings(); broken > 0; broken = breakAllBindings(), attempts++) {
                    if (attempts > 5) {
                        throw newCloseDidNotCompleteException();
                    }
                }
            } finally {
                ofNullable(closeRepository).ifPresent( close -> {
                    closeRepository = null;
                    close.close();
                });

            }
        }
    }

    @Override
    public <T> T claim(Contract<T> contract) {
        final Contract<T> validContract = contractCheck(contract);
        
        final Object deliverable = getFromPromisorMap(validContract)
            .orElseThrow(() -> newContractNotPromisedException(validContract))
            .demand();
        
        return validContract.cast(deliverable);
    }
    
    @Override
    public <T> boolean isBound(Contract<T> contract) {
        final Contract<?> validContract = contractCheck(contract);
        
        return getFromPromisorMap(validContract).isPresent();
    }
    
    @Override
    public <T> AutoClose bind(Contract<T> contract, Promisor<T> promisor) {
        final Contract<T> validContract = contractCheck(contract);
        final Promisor<T> validPromisor = promisorCheck(promisor);
        
        checkNewBinding(validContract, validPromisor);
        
        return makeBinding(validContract, validPromisor);
    }
    
    private final IdempotentImpl openState = new IdempotentImpl();
    private final ReentrantReadWriteLock mapLock = new ReentrantReadWriteLock();
    private final LinkedHashMap<Contract<?>, Promisor<?>> promisorMap = new LinkedHashMap<>();
    private final RepositoryImpl respository = new RepositoryImpl(this);
    private AutoClose closeRepository;
    
    ContractsImpl(Contracts.Config config) {
        final Contracts.Config validConfig = nullCheck(config, "config was null");
        
        // keeping the promises open permanently
        respository.store(Promisors.CONTRACT, PromisorsImpl::new);
        respository.store(Repository.FACTORY, () -> () -> new RepositoryImpl(this));
        
        if (validConfig.useShutdownHooks()) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        }
    }
    
    private void checkNewBinding(Contract<?> contract, Promisor<?> promisor) {
        getFromPromisorMap(contract).ifPresent(currentPromisor -> {
            // This check is required.
            // It prohibits the redundant caller from being able to release
            // the previous binding.
            // It is not benign and could create unexpected behaviours.
            if (currentPromisor == promisor) {
                throw newContractDuplicateBindException(contract);
            }
            if (!contract.isReplaceable()) {
                throw newContractNotReplaceableException(contract);
            }
        });
    }
    
    private <T> AutoClose makeBinding(Contract<T> contract, Promisor<T> promisor) {
        // Since ReentrantReadWriteLock does not support lock upgrade, there are opportunities
        // for changes by other threads between the reads and writes.
        // This is mitigated by always incrementing the new value and decrementing the old value.
        promisor.incrementUsage();
        return applyWithLock(mapLock.writeLock(), () -> {
            ofNullable(promisorMap.put(contract, promisor)).ifPresent(Promisor::decrementUsage);
            
            final IdempotentImpl breakBindingOnce = new IdempotentImpl();
            breakBindingOnce.transitionToOpen();
            return () -> {
              if (breakBindingOnce.transitionToClosed()) {
                  breakBinding(contract, promisor);
              }
            };
        });
    }
    
    private void breakBinding(Contract<?> contract, Promisor<?> promisor) {
        // it is possible the Contract has already been removed or updated with a new Promisor
        // Checking the removed promisor is required to avoid:
        //   1. Calling decrementUsage twice on Promisors already removed
        //   2. Not calling decrementUsage enough times
        // decrementing usage too many times.
        try {
            removeFromPromisorMap(contract, promisor);
        } finally {
            promisor.decrementUsage();
        }
    }
    
    private void removeFromPromisorMap(Contract<?> contract, Promisor<?> promisor) {
        applyWithLock(mapLock.writeLock(), () -> promisorMap.remove(contract, promisor));
    }
    
    private <T> Optional<Promisor<?>> getFromPromisorMap(Contract<T> validContract) {
        return ofNullable(applyWithLock(mapLock.readLock(), () -> promisorMap.get(validContract)));
    }
    
    private int breakAllBindings() {
        final Stack<Contract<?>> contracts = new Stack<>();
        final Stack<Promisor<?>> promisors = new Stack<>();
        
        final int contractCount = copyBindings(contracts, promisors);
        
        while (!contracts.isEmpty()) {
            breakBinding(contracts.pop(), promisors.pop());
        }
        return contractCount;
    }
    
    private int copyBindings(Stack<Contract<?>> contracts, Stack<Promisor<?>> promisors) {
        // During shutdown other threads should be able to acquire read and write locks
        // The following attains the write lock to attain all the current keys and values
        // in the reverse order from insertion.
        // The last to be inserted is the first to be removed.
        final AtomicInteger contractCount = new AtomicInteger();
        return applyWithLock(mapLock.writeLock(), () -> {
            promisorMap.forEach((contract, promisor) -> {
                contracts.push(contract);
                promisors.push(promisor);
                contractCount.incrementAndGet();
            });
            return contractCount.get();
        });
    }
    
    private <T> T applyWithLock(Lock requestedLock, Supplier<T> block) {
        requestedLock.lock();
        try {
            return block.get();
        } finally {
            requestedLock.unlock();
        }
    }
    
    private static ContractException newCloseDidNotCompleteException() {
        return new ContractException("Contracts failed to close after trying multiple times");
    }
    
    private static <T> ContractException newContractNotPromisedException(Contract<T> contract) {
        return new ContractException("Contract " + contract + " was not promised");
    }
    
    private static <T> ContractException newContractNotReplaceableException(Contract<T> contract) {
        return new ContractException("Contract " + contract + " is not replaceable");
    }
    
    private static <T> ContractException newContractDuplicateBindException(Contract<T> contract) {
        return new ContractException("Contract " + contract + " duplicated bindings not allowed");
    }
}
