package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.ServiceFactory;
import io.github.jonloucks.contracts.api.Service;

/**
 * Used to introduce errors.
 * 1. Class is not public
 * 2. createService throws an exception
 * 3. Constructor is not public
 * @see BadServiceFactoryTests
 */
final class BadServiceFactory implements ServiceFactory {
    @Override
    public Service createService(Service.Config config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    BadServiceFactory() {
    }
}
