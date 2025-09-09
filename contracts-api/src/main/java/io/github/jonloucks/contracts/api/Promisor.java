package io.github.jonloucks.contracts.api;

@FunctionalInterface
public interface Promisor<T> {
    
    T demand();
    
    default int incrementUsage() {
        return 1;
    }
    
    default int decrementUsage() {
        return 1;
    }
}
