package io.github.jonloucks.contracts.api;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.ServiceLoader;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class ServiceFactoryFinder {
    private final Service.Config config;
    
    ServiceFactoryFinder(Service.Config config) {
        this.config = nullCheck(config, "config was null");
    }
    
    ServiceFactory createServiceFactory() {
        return createByReflection()
            .or(this::createByServiceLoader)
            .orElseThrow(this::newNotFoundException);
    }

    private Optional<? extends ServiceFactory> createByServiceLoader() {
        if (config.useServiceLoader()) {
            try {
                final Class<? extends ServiceFactory> bootstrapClass = nullCheck(config.serviceLoaderClass(), "bootstrapClass was null");
                final ServiceLoader<? extends ServiceFactory> serviceLoader = ServiceLoader.load(bootstrapClass);
                return serviceLoader.findFirst();
            } catch (Throwable thrown) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Optional<ServiceFactory> createByReflection() {
        if (config.useReflection()) {
            final String className = nullCheck(config.reflectionClassName(), "config.bootstrapClassName() was null");
            if (className.isEmpty()) {
                return Optional.empty();
            }
            try {
                final Class<?> bootstrapClass = Class.forName(className);
                final Constructor<?> bootstrapConstructor = bootstrapClass.getConstructor();
                return Optional.of((ServiceFactory) bootstrapConstructor.newInstance());
            } catch (Throwable thrown) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private ContractException newNotFoundException() {
        return new ContractException("Unable to find Contracts ServiceFactory");
    }
}
