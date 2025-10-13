package io.github.jonloucks.contracts.api;

/**
 * Binding strategy
 */
public enum BindStrategy {
    /**
     * Ensure the stored promisor is always bound to the given contract or else throws and error
     */
    ALWAYS,
    /**
     * Only bind the contract if not already bound
     */
    IF_NOT_BOUND,
    /**
     * Bind the contract if not bound or the contract is replaceable
     */
    IF_ALLOWED
}
