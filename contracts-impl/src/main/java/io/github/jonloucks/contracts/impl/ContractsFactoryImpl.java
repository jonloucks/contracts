package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

/**
 * Bootstrap the Contracts library
 */
public final class ContractsFactoryImpl implements ContractsFactory {
    
    @Override
    public Contracts create(Contracts.Config config) {
        final Contracts.Config validConfig = nullCheck(config, "config");
        
        return new ContractsImpl(validConfig);
    }
}
