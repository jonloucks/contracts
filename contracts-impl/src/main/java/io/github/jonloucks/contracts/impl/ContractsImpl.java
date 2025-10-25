package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.*;
import static java.util.Optional.ofNullable;

/**
 * Implementation for {@link io.github.jonloucks.contracts.api.Contracts}
 * @see io.github.jonloucks.contracts.api.Contracts
 */
final class ContractsImpl implements Contracts {

    @Override
    public AutoClose open() {
        if (openState.transitionToOpen()) {
            closeRepository = repository.open();
            return this::close;
        }
        return AutoClose.NONE;
    }

    @Override
    public <T> T claim(Contract<T> contract) {
        final Contract<T> validContract = contractCheck(contract);
        final Optional<Promisor<?>> promisor = getFromPromisorMap(validContract);
        
        if (promisor.isPresent()) {
            return validContract.cast(promisor.get().demand());
        } else {
            return claimFromPartners(validContract);
        }
    }
    
    @Override
    public <T> boolean isBound(Contract<T> contract) {
        final Contract<?> validContract = contractCheck(contract);
        final Optional<Promisor<?>> promisor = getFromPromisorMap(validContract);
        
        return promisor.isPresent() || isAnyPartnerBound(contract);
    }
    
    @Override
    public <T> AutoClose bind(Contract<T> contract, Promisor<T> promisor, BindStrategy bindStrategy) {
        final Contract<T> validContract = contractCheck(contract);
        final Promisor<T> validPromisor = promisorCheck(promisor);
        final BindStrategy validBindStrategy = nullCheck(bindStrategy, "Bind strategy must be present.");
        
        return maybeBind(validContract, validPromisor, validBindStrategy);
    }
    
    ContractsImpl(Contracts.Config config) {
        final Contracts.Config validConfig = configCheck(config);
        
        // keeping the promises open permanently
        repository.keep(Promisors.CONTRACT, PromisorsImpl::new);
        repository.keep(Repository.FACTORY, () -> () -> new RepositoryImpl(this));
        
        partners.addAll(nullCheck(validConfig.getPartners(), "Partners must be present."));
        
        if (validConfig.useShutdownHooks()) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        }
    }
    
    private void close() {
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
    
    private <T> AutoClose maybeBind(Contract<T> contract, Promisor<T> newPromisor, BindStrategy bindStrategy) {
        if (checkBind(contract, newPromisor, bindStrategy)) {
            return doBind(contract, newPromisor);
        } else {
            return AutoClose.NONE;
        }
    }
    
    private boolean checkBind(Contract<?> contract, Promisor<?> newPromisor, BindStrategy bindStrategy) {
        final Optional<Promisor<?>> optionalCurrent = getFromPromisorMap(contract);
        
        //noinspection OptionalIsPresent
        if (optionalCurrent.isPresent()) {
            return checkReplacement(contract, newPromisor, bindStrategy, optionalCurrent.get());
        } else {
            return true;
        }
    }
    
    private static boolean checkReplacement(Contract<?> contract, Promisor<?> newPromisor, BindStrategy bindStrategy, Promisor<?> currentPromisor) {
        // Double bind of same promisor, do not rebind
        if (currentPromisor == newPromisor) {
            return false;
        }
        
        switch (bindStrategy) {
            case ALWAYS:
                if (contract.isReplaceable()) {
                    return true;
                }
                throw newContractNotReplaceableException(contract);
            case IF_NOT_BOUND:
                return false;
            case IF_ALLOWED:
            default:
                return contract.isReplaceable();
        }
    }
    
    private <T> AutoClose doBind(Contract<T> contract, Promisor<T> promisor) {
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
    
    private <T> T claimFromPartners(Contract<T> contract) {
        if (!partners.isEmpty()) {
            for (Contracts partner : partners) {
                if (partner.isBound(contract)) {
                    return partner.claim(contract);
                }
            }
        }
        throw newContractNotPromisedException(contract);
    }
    
    private <T> boolean isAnyPartnerBound(Contract<T> contract) {
        if (!partners.isEmpty()) {
            return partners.stream().anyMatch(partner -> partner.isBound(contract));
        }
        return false;
    }
    
    private static <T> T applyWithLock(Lock requestedLock, Supplier<T> block) {
        requestedLock.lock();
        try {
            return block.get();
        } finally {
            requestedLock.unlock();
        }
    }
    
    private static ContractException newCloseDidNotCompleteException() {
        return new ContractException("Contracts failed to close after trying multiple times.");
    }
    
    private static <T> ContractException newContractNotPromisedException(Contract<T> contract) {
        return new ContractException("Contract " + contract + " was not promised.");
    }
    
    private static <T> ContractException newContractNotReplaceableException(Contract<T> contract) {
        return new ContractException("Contract " + contract + " is not replaceable.");
    }

    private final IdempotentImpl openState = new IdempotentImpl();
    private final ReentrantReadWriteLock mapLock = new ReentrantReadWriteLock();
    private final LinkedHashMap<Contract<?>, Promisor<?>> promisorMap = new LinkedHashMap<>();
    private final RepositoryImpl repository = new RepositoryImpl(this);
    private final List<Contracts> partners = new ArrayList<>();
    private AutoClose closeRepository;
}
