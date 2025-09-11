package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.ServiceFactory;
import io.github.jonloucks.contracts.api.Service;

final class BadServiceFactory implements ServiceFactory {
    @Override
    public Service createService(Service.Config config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    BadServiceFactory() {
    }
}
