package io.github.jonloucks.contracts.api;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.ServiceLoader;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class ContractsFactoryFinder {
    private final Contracts.Config config;
    
    ContractsFactoryFinder(Contracts.Config config) {
        this.config = nullCheck(config, "config was null");
    }
    
    ContractsFactory find() {
        return createByReflection()
            .or(this::createByServiceLoader)
            .orElseThrow(this::newNotFoundException);
    }

    private Optional<? extends ContractsFactory> createByServiceLoader() {
        if (config.useServiceLoader()) {
            try {
                final Class<? extends ContractsFactory> factoryClass = nullCheck(config.serviceLoaderClass(), "config.serviceLoaderClass() was null");
                final ServiceLoader<? extends ContractsFactory> serviceLoader = ServiceLoader.load(factoryClass);
                return serviceLoader.findFirst();
            } catch (Throwable thrown) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Optional<ContractsFactory> createByReflection() {
        if (config.useReflection()) {
            final String className = nullCheck(config.reflectionClassName(), "config.reflectionClassName() was null");
            if (className.isEmpty()) {
                return Optional.empty();
            }
            try {
                final Class<?> bootstrapClass = Class.forName(className);
                final Constructor<?> bootstrapConstructor = bootstrapClass.getConstructor();
                return Optional.of((ContractsFactory) bootstrapConstructor.newInstance());
            } catch (Throwable thrown) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private ContractException newNotFoundException() {
        return new ContractException("Unable to find GlobalContracts ContractsFactory");
    }
}
