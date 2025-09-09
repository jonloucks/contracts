package io.github.jonloucks.contracts.api;

/**
 * Responsible to provide service creation
 */
public interface ServiceFactory {
    
    Service createService(Service.Config config);
}
