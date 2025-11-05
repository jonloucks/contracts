package io.github.jonloucks.contracts.api;

import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.BindStrategy.IF_ALLOWED;

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
     * Note: Replacing a Contract already promised in this Repository is forbidden after the Repository is opened.
     * If the Repository is not open an existing Promisor can be replaced, otherwise it is forbidden.
     * If the Repository is not open, the binding will be applied when repository is opened.
     * If the Repository has already been opened the binding is applied immediately
     * Note: If never explicitly closed, the order of closing promisors is the reverse order they are stored
     * @param contract the contract to be bound
     * @param promisor the promisor to be bounded
     * @param bindStrategy the config for storing the binding
     * @return AutoClose responsible for removing the binding from this Repository
     * @param <T> the type of contract deliverable
     */
    <T> AutoClose store(Contract<T> contract, Promisor<T> promisor, BindStrategy bindStrategy);

    /**
     * Store the binding.
     * Note: Replacing a Contract already promised in this Repository is forbidden after the Repository is opened.
     * If the Repository is not open an existing Promisor can be replaced, otherwise it is forbidden.
     * If the Repository is not open, the binding will be applied when repository is opened.
     * If the Repository has already been opened the binding is applied immediately
     * Note: If never explicitly closed, the order of closing promisors is the reverse order they are stored
     * @param contract the contract to be bound
     * @param promisor the promisor to be bounded
     * @return AutoClose responsible for removing the binding from this Repository
     * @param <T> the type of contract deliverable
     */
    default <T> AutoClose store(Contract<T> contract, Promisor<T> promisor) {
        return store(contract, promisor, IF_ALLOWED);
    }
    
    /**
     * Keep the binding for the life of the repository
     * If the Repository is not open, the binding will be created when repository is opened.
     * If the Repository has already been opened the binding is created immediately
     * Note: The order of closing promisors is the reverse order they are stored
     * @param contract the contract to be bound
     * @param promisor the promisor to be bounded
     * @param bindStrategy the config for storing the binding

     * @param <T> the type of contract deliverable
     */
    default <T> void keep(Contract<T> contract, Promisor<T> promisor, BindStrategy bindStrategy) {
        //noinspection resource; all repository promises are closed when the repository is closed
        store(contract, promisor, bindStrategy);
    }
    
    /**
     * Keep the binding for the life of the repository
     * If the Repository is not open, the binding will be created when repository is opened.
     * If the Repository has already been opened the binding is created immediately
     * Note: The order of closing promisors is the reverse order they are stored
     * @param contract the contract to be bound
     * @param promisor the promisor to be bounded
     * @param <T> the type of contract deliverable
     */
     default <T> void keep(Contract<T> contract, Promisor<T> promisor) {
         keep(contract, promisor, IF_ALLOWED);
     }
    
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
