package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;

public interface Decoy<D> extends Promisor<D>, ContractsFactory, Contracts, AutoOpen, AutoCloseable {
    @Override
    default AutoClose open() { return this;}

    @Override
    default void close() {
    }
    
    @Override
    default <T> T claim(Contract<T> contract) {
        return null;
    }
    
    @Override
    default <T> boolean isBound(Contract<T> contract) {
        return false;
    }
    
    @Override
    default <T> AutoClose bind(Contract<T> contract, Promisor<T> promisor) {
        return () -> {};
    }
    
    @Override
    default D demand() {
        return null;
    }
    
    @Override
    default Contracts create(Contracts.Config config) {
        throw new ContractException("Decoy.create not implemented");
    }
}
