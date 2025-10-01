package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.Promisor;

import java.util.function.Function;

import static io.github.jonloucks.contracts.api.Checks.*;

/**
 * Implementation for {@link io.github.jonloucks.contracts.api.Promisors#createExtractPromisor(Promisor, Function)}
 * @see io.github.jonloucks.contracts.api.Promisors#createExtractPromisor(Promisor, Function)
 * @param <T> the input deliverable type
 * @param <R> the output deliverable type
 */
final class ExtractPromisorImpl<T, R> implements Promisor<R> {
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
   
    ExtractPromisorImpl(Promisor<T> referent, Function<T, R> transform) {
        this.referent = promisorCheck(referent);
        this.transform = nullCheck(transform, "transform was null");
    }
    
    private final Promisor<T> referent;
    private final Function<T, R> transform;
}
