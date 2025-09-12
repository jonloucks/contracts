package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

/**
 * Bootstrap the Contracts library
 */
public final class ServiceFactoryImpl implements ServiceFactory {
    
    @Override
    public Service createService(Service.Config config) {
        final Service.Config validConfig = nullCheck(config, "config");
        
        return new ServiceImpl(validConfig);
    }
}
