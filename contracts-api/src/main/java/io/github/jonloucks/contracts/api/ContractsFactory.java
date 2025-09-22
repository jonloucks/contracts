package io.github.jonloucks.contracts.api;

/**
 * A Contracts factory to bootstrapping GlobalContracts and provide
 * Standalone services for special needs.
 */
public interface ContractsFactory {
    
    /**
     * Create a new Contracts
     * Note: Caller is responsible for invoking 'startup' before using methods
     * Note: If caller does invoke 'open' it is required to invoke 'close' when appropriate
     * @param config the Contracts configuration
     * @return the new Contracts
     */
    Contracts create(Contracts.Config config);
}
