package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.*;

import static io.github.jonloucks.contracts.api.Checks.configCheck;

/**
 * Implementation for {@link io.github.jonloucks.contracts.api.ContractsFactory}
 * @see io.github.jonloucks.contracts.api.ContractsFactory
 */
public final class ContractsFactoryImpl implements ContractsFactory {
    
    @Override
    public Contracts create(Contracts.Config config) {
        final Contracts.Config validConfig = configCheck(config);
        
        return new ContractsImpl(validConfig);
    }
}
