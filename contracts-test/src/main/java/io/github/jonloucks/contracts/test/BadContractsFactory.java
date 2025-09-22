package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.ContractsFactory;
import io.github.jonloucks.contracts.api.Contracts;

/**
 * Used to introduce errors.
 * 1. Class is not public
 * 2. create throws an exception
 * 3. Constructor is not public
 * @see BadContractsFactoryTests
 */
final class BadContractsFactory implements ContractsFactory {
    @Override
    public Contracts create(Contracts.Config config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    BadContractsFactory() {
    }
}
