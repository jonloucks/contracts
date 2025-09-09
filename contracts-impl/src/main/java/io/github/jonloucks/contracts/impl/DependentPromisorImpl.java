package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.Promisor;

import java.util.function.Function;

import static io.github.jonloucks.contracts.api.Checks.*;

final class DependentPromisorImpl<T,R> implements Promisor<R> {
    @Override
    public R demand() {
        return transform.apply(referent.demand());
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
    private final Function<T, R> transform;
    
    DependentPromisorImpl(Promisor<T> referent, Function<T, R> transform) {
        this.referent = promisorCheck(referent);
        this.transform = nullCheck(transform, "transform was null");
    }
}
