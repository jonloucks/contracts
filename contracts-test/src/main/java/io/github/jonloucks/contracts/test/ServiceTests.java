package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings({"RedundantMethodOverride"})
public interface ServiceTests {
    
    @Test
    default void service_DefaultConfig() {
        final Service.Config config = new Service.Config() {};
        
        assertAll(
            () -> assertTrue(config.useReflection(), "config.useReflection() default."),
            () -> assertTrue(config.useServiceLoader(), "config.useServiceLoader() default."),
            () -> assertTrue(config.useShutdownHooks(), "config.useShutdownHooks() default."),
            () -> assertNotNull(config.reflectionClassName(), "config.reflectionClassName() was null.")
        );
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.contracts.test.ServiceTests$ServiceTestsTools#validConfigs")
    default void service_HappyPath(Service.Config serviceConfig) {
        final Contract<String> contract = Contract.create("string contract");
        final Contract<String> unboundContract = Contract.create("unbound string contract");
        
        final Service service = Contracts.createService(serviceConfig);
        
        assumeTrue(ofNullable(service).isPresent(), "create service failed");
        
        try (AutoClose closeService = service.open()) {
            nullCheck(closeService, "warning: [try] workaround");
            
            service.bind(contract, () -> "hello");
            
            assertFalse(service.isBound(unboundContract), "Contract should be bound.");
            assertTrue(service.isBound(contract), "Contract should be bound.");
            assertEquals("hello", service.claim(contract), "Claimed value should match.");
        }
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.contracts.test.ServiceTests$ServiceTestsTools#invalidConfigs")
    default void service_SadPath(Service.Config serviceConfig) {
        final ContractException thrown = assertThrows(ContractException.class, () -> {
            //noinspection resource
            Contracts.createService(serviceConfig);
        });
        
        assertThrown(thrown);
    }
    
    final class ServiceTestsTools {
        private ServiceTestsTools() {
        
        }
        static Stream<Arguments> validConfigs() {
            return Stream.of(
                Arguments.of(new Service.Config() {}),
                Arguments.of(new Service.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return true;
                    }
                }),
                Arguments.of(new Service.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return true;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                })
            );
        }
        
        static Stream<Arguments> invalidConfigs() {
            return Stream.of(
                Arguments.of(new Service.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                }),
                Arguments.of(new Service.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return true;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                    @Override
                    public Class<? extends ServiceFactory> serviceLoaderClass() {
                        return BadServiceFactory.class;
                    }
                }),
                Arguments.of(new Service.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return true;
                    }
                    @Override
                    public String reflectionClassName() {
                        return BadServiceFactory.class.getName();
                    }
                }),
                Arguments.of(new Service.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return true;
                    }
                    @Override
                    public String reflectionClassName() {
                        return "";
                    }
                })
            );
        }
    }
}
