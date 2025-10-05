package io.github.jonloucks.contracts.api;

import java.util.function.Supplier;

/**
 * A repository for multiple contract promisors
 * This is an opt-in feature to simplify the managing of many contract bindings.
 * 1. Optional feature to register required contracts.
 * 2. Optional feature to manage multiple contract bindings.
 */
public interface Repository extends AutoOpen {
    
    /**
     * Contract to deliver a Repository factory
     */
    Contract<Supplier<Repository>> FACTORY = Contract.create("Repository Factory");
    
    /**
     * Store the binding.
     * If the Repository is not open, the binding will be created when repository is opened.
     * If the Repository has already been opened the binding is created immediately
     * @param contract the contract to be bound
     * @param promisor the promisor to be bounded
     * @return AutoClose responsible for removing the binding from this Repository
     * @param <T> the type of contract deliverable
     */
    <T> AutoClose store(Contract<T> contract, Promisor<T> promisor);
    
    /**
     * Check that all requirements have fulfilled
     */
    void check();
    
    /**
     * Added a required contract
     * @param contract the contract to be required
     * @param <T> the type of contract deliverable
     */
    <T> void require(Contract<T> contract);
}
