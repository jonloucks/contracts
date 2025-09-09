package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.Promisor;
import io.github.jonloucks.contracts.api.Shutdown;
import io.github.jonloucks.contracts.api.Startup;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.*;
import static java.util.Optional.ofNullable;

final class LifeCyclePromisorImpl<T> implements Promisor<T> {
    
    @Override
    public T demand() {
        if (usageCounter.get() == 0) {
            throw new IllegalStateException("Usage count is zero");
        }
        final Optional<Supplier<T>> optionalDeliverableSupplier = getOptionalDeliverableSupplier();
        if (optionalDeliverableSupplier.isPresent()) {
            return optionalDeliverableSupplier.get().get();
        }
        return createDeliverable();
    }
    
    @Override
    public int incrementUsage() {
        final int currentUsage = usageCounter.incrementAndGet();
        referentPromisor.incrementUsage();
        return currentUsage;
    }
    
    @Override
    public int decrementUsage() {
        final int currentUsage = usageCounter.decrementAndGet();
        try {
            if (currentUsage == 0) {
                shutdownDeliverable();
            }
        } finally {
            referentPromisor.decrementUsage();
        }
        return currentUsage;
    }
    
    private final AtomicInteger usageCounter = new AtomicInteger();
    private final Promisor<T> referentPromisor;
    private final AtomicBoolean isDeliverableAcquired = new AtomicBoolean();
    private final AtomicReference<T> atomicDeliverable = new AtomicReference<>();
    private final AtomicReference<Throwable> startupException = new AtomicReference<>();
    private final Object simpleLock = new Object();
    
    LifeCyclePromisorImpl(Promisor<T> referentPromisor) {
        this.referentPromisor = promisorCheck(referentPromisor);
    }
    
    private Optional<Supplier<T>> getOptionalDeliverableSupplier() {
        ofNullable(startupException.get())
            .ifPresent(this::reThrowStartupException);
        if (isDeliverableAcquired.get()) {
            final T savedDeliverable = atomicDeliverable.get();
            return Optional.of(() -> savedDeliverable);
        }
        return Optional.empty();
    }
    
    private void reThrowStartupException(Throwable thrown) {
        if (thrown instanceof Error) {
            throw (Error) thrown;
        }
        if (thrown instanceof RuntimeException) {
            throw (RuntimeException) thrown;
        }
        throw new IllegalStateException("Startup", thrown);
    }
    
    private T createDeliverable() {
        synchronized (simpleLock) {
            if (!isDeliverableAcquired.get()) {
                final T currentDeliverable = referentPromisor.demand();
                atomicDeliverable.set(currentDeliverable);
                startupDeliverable(currentDeliverable);
                isDeliverableAcquired.set(true);
            }
            return atomicDeliverable.get();
        }
    }
    
    private void startupDeliverable(final T deliverable) {
        if (deliverable instanceof Startup) {
            synchronized (simpleLock) {
                try {
                    ((Startup) deliverable).startup();
                } catch (Throwable thrown) {
                    if (atomicDeliverable.compareAndSet(deliverable, null)) {
                        startupException.set(thrown);
                        isDeliverableAcquired.set(false);
                    }
                }
            }
        }
    }
 
    private void shutdownDeliverable() {
        synchronized (simpleLock) {
            if (isDeliverableAcquired.get()) {
                final T deliverable = atomicDeliverable.get();
                try {
                    if (deliverable instanceof Shutdown) {
                        ((Shutdown) deliverable).shutdown();
                    }
                } finally {
                    atomicDeliverable.compareAndSet(deliverable, null);
                    isDeliverableAcquired.set(false);
                }
            }
        }
    }
}
