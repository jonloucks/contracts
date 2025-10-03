package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.contracts.api.Promisor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.jonloucks.contracts.api.Checks.*;
import static java.util.Optional.ofNullable;

/**
 * Implementation for {@link io.github.jonloucks.contracts.api.Promisors#createLifeCyclePromisor(Promisor)}
 * @see io.github.jonloucks.contracts.api.Promisors#createLifeCyclePromisor(Promisor)
 * @param <T> the type of deliverable
 */
final class LifeCyclePromisorImpl<T> implements Promisor<T> {
    
    @Override
    public T demand() {
        final AtomicReference<T> currentDeliverable = new AtomicReference<>();
        if (getCurrentDeliverable(currentDeliverable)) {
            return currentDeliverable.get();
        }
        return createDeliverableIfNeeded();
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
                closeDeliverable();
            }
        } finally {
            referentPromisor.decrementUsage();
        }
        return currentUsage;
    }
    
    LifeCyclePromisorImpl(Promisor<T> referentPromisor) {
        this.referentPromisor = promisorCheck(referentPromisor);
    }
    
    private boolean getCurrentDeliverable(AtomicReference<T> placeholder) {
        if (usageCounter.get() == 0) {
            throw new IllegalStateException("Usage count is zero");
        }
        maybeRethrowOpenException();
        synchronized (simpleLock) {
            if (isDeliverableAcquired.get()) {
                placeholder.set(atomicDeliverable.get());
                return true;
            }
            return false;
        }
    }
    
    private void maybeRethrowOpenException() {
        ofNullable(openException.get())
            .ifPresent(this::rethrowOpenException);
    }
    
    private void rethrowOpenException(Throwable thrown) {
        if (thrown instanceof Error) {
            throw (Error) thrown;
        }
        if (thrown instanceof RuntimeException) {
            throw (RuntimeException) thrown;
        }
        throw new IllegalStateException(thrown);
    }
    
    private T createDeliverableIfNeeded() {
        synchronized (simpleLock) {
            if (isDeliverableAcquired.get()) {
                return atomicDeliverable.get();
            } else {
                return createDeliverable();
            }
        }
    }
    
    private T createDeliverable() {
        openException.set(null);
        final T currentDeliverable = referentPromisor.demand();
        atomicDeliverable.set(currentDeliverable);
        openDeliverable(currentDeliverable);
        isDeliverableAcquired.set(true);
        return currentDeliverable;
    }
    
    private void openDeliverable(final T deliverable) {
        if (deliverable instanceof AutoOpen) {
            synchronized (simpleLock) {
                try {
                    atomicClose.set(((AutoOpen) deliverable).open());
                } catch (Throwable thrown) {
                    if (atomicDeliverable.compareAndSet(deliverable, null)) {
                        openException.set(thrown);
                        isDeliverableAcquired.set(false);
                    }
                    throw thrown;
                }
            }
        }
    }
    
    private void closeDeliverable() {
        if (isDeliverableAcquired.get()) {
            final T deliverable = atomicDeliverable.get();
            try {
                ofNullable(atomicClose.get()).ifPresent(close -> {
                    atomicClose.set(null);
                    close.close();
                });
            } finally {
                atomicDeliverable.compareAndSet(deliverable, null);
                isDeliverableAcquired.set(false);
            }
        }
    }
    
    private final AtomicInteger usageCounter = new AtomicInteger();
    private final Promisor<T> referentPromisor;
    private final AtomicBoolean isDeliverableAcquired = new AtomicBoolean();
    private final AtomicReference<T> atomicDeliverable = new AtomicReference<>();
    private final AtomicReference<Throwable> openException = new AtomicReference<>();
    private final AtomicReference<AutoClose> atomicClose = new AtomicReference<>();
    private final Object simpleLock = new Object();
}
