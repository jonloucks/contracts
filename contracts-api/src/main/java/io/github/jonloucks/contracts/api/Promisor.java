package io.github.jonloucks.contracts.api;

/**
 * Interface for providing a deliverable for a Contract
 * The main and required implementation is {@link #demand()}
 * There are optional methods with appropriate defaults.
 * @param <T> The type of the deliverable
 */
@FunctionalInterface
public interface Promisor<T> {
    
    /**
     * Return the deliverable promised for a Contract
     * @return the current deliverable
     */
    T demand();
    
    /**
     * Reference counting used for advanced resource management
     * Incremented by {@link Contracts#bindContract(Contract, Promisor)}
     * Decremented if caller invokes {@link AutoClose#close()} on the return value of bind
     * Every successful 'open' must be followed by a 'close' at the appropriate time
     * @return the usage count.  This might be a constant
     */
    default int incrementUsage() {
        return 1;
    }
    
    /**
     * Reference counting used for advanced resource management
     * Incremented by {@link Contracts#bindContract(Contract, Promisor)}
     * Decremented if caller invokes {@link AutoClose#close()} on the return value of bind
     * Every successful 'open' must be followed by a 'close' at the appropriate time
     * @return the usage count.  This might be a constant
     */
    default int decrementUsage() {
        return 1;
    }
}
