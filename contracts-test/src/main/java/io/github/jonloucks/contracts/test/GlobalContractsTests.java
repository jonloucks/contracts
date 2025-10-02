package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.jonloucks.contracts.test.Tools.*;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("CodeBlock2Expr")
public interface GlobalContractsTests {

    @Test
    default void globalContracts_Instantiate_Throws() {
        assertInstantiateThrows(GlobalContracts.class);
    }
    
    @Test
    default void globalContracts_getInstance_Works() {
        assertObject(GlobalContracts.getInstance());
    }
    
    @Test
    default void globalContracts_claimContract_WithNullContract_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->  {
            GlobalContracts.claimContract(null);
        });

        assertThrown(thrown);
    }
    
    @Test
    default void globalContracts_claimContract_WithUnknownContract_Throws() {
        final Contract<String> contract = Contract.create("testContract");
        final ContractException thrown = assertThrows(ContractException.class, () ->  {
            GlobalContracts.claimContract(contract);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void globalContracts_claimContract_WithPromisedContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        try (AutoClose releaseBinding = GlobalContracts.bindContract(contract, () -> "abc")){
            assertAll(
                () -> assertNotNull(releaseBinding),
                () -> assertSame("abc", GlobalContracts.claimContract(contract)));
        }
    }
    
    @Test
    default void globalContracts_isContractBound_WithUnboundContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        
        assertFalse(GlobalContracts.isContractBound(contract), "Unbound Contract was bound.");
    }
    
    @Test
    default void globalContracts_isContractBound_WithBoundContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        
        try (AutoClose closeBinding = GlobalContracts.bindContract(contract, () -> "abc")){
            final AutoClose ignored = closeBinding;
            assertTrue(GlobalContracts.isContractBound(contract), "Unbound Contract was bound.");
        }
    }
    
    
    @Test
    default void globalContracts_DefaultConfig() {
        final Contracts.Config config = new Contracts.Config() {};
        
        assertAll(
            () -> assertTrue(config.useReflection(), "config.useReflection() default."),
            () -> assertTrue(config.useServiceLoader(), "config.useServiceLoader() default."),
            () -> assertTrue(config.useShutdownHooks(), "config.useShutdownHooks() default."),
            () -> assertNotNull(config.reflectionClassName(), "config.reflectionClassName() was null.")
        );
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.contracts.test.GlobalContractsTests$GlobalContractsTestsTools#validConfigs")
    default void globalContracts_HappyPath(Contracts.Config contractsConfig) {
        final Contract<String> contract = Contract.create("string contract");
        final Contract<String> unboundContract = Contract.create("unbound string contract");
        final Contracts contracts = GlobalContracts.createContracts(contractsConfig);
        
        assumeTrue(ofNullable(contracts).isPresent(), "createContracts failed");
        
        try (AutoClose closeContracts = contracts.open()) {
            final AutoClose ignored = closeContracts;
            
            try (AutoClose closeBinding = contracts.bind(contract, () -> "hello")) {
                final AutoClose ignoredBinding = closeBinding;
                assertFalse(contracts.isBound(unboundContract), "Contract should be bound.");
                assertTrue(contracts.isBound(contract), "Contract should be bound.");
                assertEquals("hello", contracts.claim(contract), "Claimed value should match.");
            }
        }
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.contracts.test.GlobalContractsTests$GlobalContractsTestsTools#invalidConfigs")
    default void globalContracts_SadPath(Contracts.Config contractsConfig) {
        final ContractException thrown = assertThrows(ContractException.class, () -> {
            GlobalContracts.createContracts(contractsConfig);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void globalContracts_InternalCoverage() {
        assertInstantiateThrows(GlobalContractsTestsTools.class);
    }
    
    @SuppressWarnings("RedundantMethodOverride")
    final class GlobalContractsTestsTools {
        private GlobalContractsTestsTools() {
            throw new AssertionError("Illegal constructor");
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
