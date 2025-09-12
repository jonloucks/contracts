package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.Promisor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.jonloucks.contracts.api.Checks.promisorCheck;

final class SingletonPromisorImpl<T> implements Promisor<T> {
    @Override
    public T demand() {
        if (firstTime.compareAndSet(true, false)) {
            singletonRef.set(referent.demand());
        }
        return singletonRef.get();
    }
    
    @Override
    public int incrementUsage() {
        return referent.incrementUsage();
    }
    
    @Override
    public int decrementUsage() {
        return referent.decrementUsage();
    }
    
    private final Promisor<T> referent;
    private final AtomicReference<T> singletonRef = new AtomicReference<>();
    private final AtomicBoolean firstTime = new AtomicBoolean(true);
    
    SingletonPromisorImpl(Promisor<T> referent) {
        this.referent = promisorCheck(referent);
    }
}
