package io.github.jonloucks.contracts.api;

import java.util.function.Function;

public interface Promisors {
    Contract<Promisors> CONTRACT = Contract.create("Promisors");
    
    <T> Promisor<T> createValuePromisor(T value);
    
    <T> Promisor<T> createLifeCyclePromisor(Promisor<T> promisor);

    <T,R> Promisor<R> createDependentPromisor(Promisor<T> promisor, Function<T, R> transform);
}
