package io.github.jonloucks.contracts.api;

/**
 * A service factory to bootstrapping Contracts and provide
 * Standalone services for special needs.
 */
public interface ServiceFactory {
    
    /**
     * Create a new Service
     * Note: Caller is responsible for invoking 'startup' before using service methods
     * Note: If caller does invoke 'open' it is required to invoke 'close' when appropriate
     * @param config the service configuration
     * @return the new Service
     */
    Service createService(Service.Config config);
}
