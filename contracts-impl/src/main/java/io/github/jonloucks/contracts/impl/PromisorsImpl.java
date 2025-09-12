package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.Promisors;
import io.github.jonloucks.contracts.api.Promisor;

import java.util.function.Function;

final class PromisorsImpl implements Promisors {
    
    @Override
    public <T> Promisor<T> createValuePromisor(T deliverable) {
        return () -> deliverable;
    }
    
    @Override
    public <T> Promisor<T> createSingletonPromisor(Promisor<T> promisor) {
        return new SingletonPromisorImpl<>(promisor);
    }
    
    @Override
    public <T> Promisor<T> createLifeCyclePromisor(Promisor<T> promisor) {
        return new LifeCyclePromisorImpl<>(promisor);
    }
    
    @Override
    public <T, R> Promisor<R> createExtractPromisor(Promisor<T> promisor, Function<T, R> extractor) {
        return new ExtractPromisorImpl<>(promisor, extractor);
    }
    
    PromisorsImpl() {
    
    }
}
