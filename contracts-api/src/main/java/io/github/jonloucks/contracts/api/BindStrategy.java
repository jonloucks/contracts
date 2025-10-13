package io.github.jonloucks.contracts.api;

/**
 * Binding strategy.
 * <p>
 * Used to dictate how or if binding should happen when the Contract is already bound.
 * </p>
 * @see Contracts#bind(Contract, Promisor, BindStrategy)
 * @see Repository#store(Contract, Promisor, BindStrategy)
 * @see Repository#keep(Contract, Promisor, BindStrategy)
 */
public enum BindStrategy {
    /**
     * Bind the new promisor to the given contract always or else throws an error.
     */
    ALWAYS,
    /**
     * Bind the new promisor to the given contract if not already bound.
     */
    IF_NOT_BOUND,
    /**
     * Bind the new promisor to the given contract only if replacement is allowed
     */
    IF_ALLOWED
}
