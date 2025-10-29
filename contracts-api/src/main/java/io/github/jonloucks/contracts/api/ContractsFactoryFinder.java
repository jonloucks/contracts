package io.github.jonloucks.contracts.api;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.ServiceLoader;

import static io.github.jonloucks.contracts.api.Checks.configCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class ContractsFactoryFinder {
    private final Contracts.Config config;
    
    ContractsFactoryFinder(Contracts.Config config) {
        this.config = configCheck(config);
    }
    
    ContractsFactory find() {
        return createByReflection().orElseGet(() -> createByServiceLoader().orElseThrow(this::newNotFoundException));
    }

    private Optional<ContractsFactory> createByServiceLoader() {
        if (config.useServiceLoader()) {
            try {
                final ServiceLoader<? extends ContractsFactory> loader = ServiceLoader.load(config.serviceLoaderClass());
                for (ContractsFactory factory : loader) {
                    return Optional.of(factory);
                }
            } catch (Throwable ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Class<? extends ContractsFactory> getServiceFactoryClass() {
        return nullCheck(config.serviceLoaderClass(), "Contracts Service Loader class must be present.");
    }
    
    private Optional<ContractsFactory> createByReflection() {
        if (config.useReflection()) {
            final String className = getClassName();
            if (className.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of((ContractsFactory) getConstructor(className).newInstance());
            } catch (Throwable ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private String getClassName() {
        return nullCheck(config.reflectionClassName(), "Reflection reflection class name must be present.");
    }
   
    private Constructor<?> getConstructor(String className) throws Throwable {
        return Class.forName(className).getConstructor();
    }
    
    private ContractException newNotFoundException() {
        return new ContractException("Unable to find Contracts factory.");
    }
}
