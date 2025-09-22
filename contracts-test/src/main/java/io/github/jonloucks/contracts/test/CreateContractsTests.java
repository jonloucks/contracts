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
public interface CreateContractsTests {
    
    @Test
    default void createContracts_DefaultConfig() {
        final Contracts.Config config = new Contracts.Config() {};
        
        assertAll(
            () -> assertTrue(config.useReflection(), "config.useReflection() default."),
            () -> assertTrue(config.useServiceLoader(), "config.useServiceLoader() default."),
            () -> assertTrue(config.useShutdownHooks(), "config.useShutdownHooks() default."),
            () -> assertNotNull(config.reflectionClassName(), "config.reflectionClassName() was null.")
        );
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.contracts.test.CreateContractsTests$CreateContractsTestsTools#validConfigs")
    default void createContracts_HappyPath(Contracts.Config contractsConfig) {
        final Contract<String> contract = Contract.create("string contract");
        final Contract<String> unboundContract = Contract.create("unbound string contract");
        
        final Contracts contracts = GlobalContracts.createContracts(contractsConfig);
        
        assumeTrue(ofNullable(contracts).isPresent(), "createContracts failed");
        
        try (AutoClose closeContracts = contracts.open()) {
            nullCheck(closeContracts, "warning: [try] workaround");
            
            contracts.bind(contract, () -> "hello");
            
            assertFalse(contracts.isBound(unboundContract), "Contract should be bound.");
            assertTrue(contracts.isBound(contract), "Contract should be bound.");
            assertEquals("hello", contracts.claim(contract), "Claimed value should match.");
        }
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.contracts.test.CreateContractsTests$CreateContractsTestsTools#invalidConfigs")
    default void createContracts_SadPath(Contracts.Config contractsConfig) {
        final ContractException thrown = assertThrows(ContractException.class, () -> {
            //noinspection resource
            GlobalContracts.createContracts(contractsConfig);
        });
        
        assertThrown(thrown);
    }
    
    final class CreateContractsTestsTools {
        private CreateContractsTestsTools() {
        
        }
        static Stream<Arguments> validConfigs() {
            return Stream.of(
                Arguments.of(new Contracts.Config() {}),
                Arguments.of(new Contracts.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return true;
                    }
                }),
                Arguments.of(new Contracts.Config() {
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
                Arguments.of(new Contracts.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                }),
                Arguments.of(new Contracts.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return true;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                    @Override
                    public Class<? extends ContractsFactory> serviceLoaderClass() {
                        return BadContractsFactory.class;
                    }
                }),
                Arguments.of(new Contracts.Config() {
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
                        return BadContractsFactory.class.getName();
                    }
                }),
                Arguments.of(new Contracts.Config() {
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
