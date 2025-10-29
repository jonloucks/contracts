package io.github.jonloucks.contracts.api;

import io.github.jonloucks.contracts.api.PunchBoard.ServiceFinder;

import java.lang.reflect.Constructor;
import java.util.Optional;

import static io.github.jonloucks.contracts.api.Checks.configCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.api.PunchBoard.SERVICE_FINDER;

final class ContractsFactoryFinder {
    private final Contracts.Config config;
    
    ContractsFactoryFinder(Contracts.Config config) {
        this.config = configCheck(config);
    }
    
    ContractsFactory find() {
        return createByReflection()
            .orElseGet(() -> createByServiceLoader()
                .orElseThrow(this::newNotFoundException));
    }

    private Optional<? extends ContractsFactory> createByServiceLoader() {
        if (config.useServiceLoader()) {
            try {
                final Optional<ServiceFinder> optionalPunch = PunchBoard.getPunch(SERVICE_FINDER);
                if (optionalPunch.isPresent()) {
                    return optionalPunch.get().findInstance(getServiceFactoryClass());
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
