package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.ServiceFactory;
import io.github.jonloucks.contracts.api.Service;

class UnknownServiceFactoryImpl implements ServiceFactory {
    @Override
    public Service createService(Service.Config config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    UnknownServiceFactoryImpl() {
    }
}
