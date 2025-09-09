package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.Promisors;
import io.github.jonloucks.contracts.api.Promisor;

import java.util.function.Function;

final class PromisorsImpl implements Promisors {
    
    @Override
    public <T> Promisor<T> createValuePromisor(T value) {
        return () -> value;
    }
    
    @Override
    public <T> Promisor<T> createLifeCyclePromisor(Promisor<T> promisor) {
        return new LifeCyclePromisorImpl<>(promisor);
    }
    
    @Override
    public <T, R> Promisor<R> createDependentPromisor(Promisor<T> promisor, Function<T, R> transform) {
        return new DependentPromisorImpl<>(promisor, transform);
    }
    
    PromisorsImpl() {
    
    }
}
