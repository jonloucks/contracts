package io.github.jonloucks.contracts.test;


import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static io.github.jonloucks.contracts.api.BindStrategy.ALWAYS;
import static io.github.jonloucks.contracts.api.BindStrategy.IF_NOT_BOUND;
import static io.github.jonloucks.contracts.test.ContractsTests.ContractsTestsTools.runWithScenario;
import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"CodeBlock2Expr"})
public interface ContractsTests {
    
    @Test
    default void contracts_claim_WithNullContract_MayThrow() {
        runWithScenario((contracts, closeContracts) -> {
            assertThrown(IllegalArgumentException.class, () -> contracts.claim(null));
        });
    }
    
    @Test
    default void contracts_claim_NotBound_Throws() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            
            assertThrown(ContractException.class, () -> contracts.claim(contract));
        });
    }
    
    @Test
    default void contracts_replaceBinding_Works() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = createReplaceableContract(Integer.class);
            
            try (AutoClose firstBinding = contracts.bind(contract, () -> 9)) {
                try (AutoClose secondBinding = contracts.bind(contract, () -> 100)) {
                    ignore(secondBinding);
                    implicitClose(firstBinding);
                    assertEquals(100, contracts.claim(contract));
                }
            }
        });
    }
    
    @Test
    default void contracts_bind_WithNullContract_MayThrow() {
        runWithScenario((contracts, closeContracts) -> {
            final Promisor<Integer> promisor = () -> 1;
            //noinspection resource
            assertThrown(IllegalArgumentException.class, () -> contracts.bind(null, promisor));
        });
    }
    
    @Test
    default void contracts_bind_WithExisting_IF_NOT_BOUND_Works() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = createReplaceableContract(Integer.class);
            
            try (AutoClose firstBinding = contracts.bind(contract, () -> 9);
                 AutoClose secondBinding = contracts.bind(contract, () -> 100, IF_NOT_BOUND)) {
                ignore(firstBinding); ignore(secondBinding);
                assertEquals(9, contracts.claim(contract));
            }
        });
    }
    
    @Test
    default void contracts_bind_WithExisting_ALWAYS_Works() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = createReplaceableContract(Integer.class);
            
            try (AutoClose firstBinding = contracts.bind(contract, () -> 9);
                 AutoClose secondBinding = contracts.bind(contract, () -> 100, ALWAYS)) {
                ignore(firstBinding); ignore(secondBinding);
                assertEquals(100, contracts.claim(contract));
            }
        });
    }
    
    @Test
    default void contracts_idempotent_DoesNotThrow() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            
            try (AutoClose closeBinding = contracts.bind(contract, () -> 9)) {
                assertDoesNotThrow(() -> {
                    implicitClose(closeBinding);
                    implicitClose(closeBinding);
                });
            }
        });
    }
    
    @Test
    default void contracts_open_twice_IsIgnored() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            
            try (AutoClose closeBinding = contracts.bind(contract, () -> 3)) {
                ignore(closeBinding);
                
                contracts.open().close();
                
                assertEquals(3, contracts.claim(contract));
            }
        });
    }
    
    @Test
    default void contracts_bind_AlreadyBoundAndNotReplaceable_Throws() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            try (AutoClose closeBinding = contracts.bind(contract, () -> 3)) {
                ignore(closeBinding);
                
                //noinspection resource
                assertThrown(ContractException.class, () -> contracts.bind(contract, () -> 4, BindStrategy.ALWAYS));
            }
        });
    }
    
    @Test
    default void contracts_bind_Twice_IsIgnored() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = createReplaceableContract(Integer.class);
            final Promisor<Integer> promisor = () -> 7;
            try (AutoClose closeBinding = contracts.bind(contract, promisor)) {
                ignore(closeBinding);
                
                contracts.bind(contract, promisor).close();
                
                assertTrue(contracts.isBound(contract));
            }
        });
    }
    
    @Test
    default void contracts_BindOnClose_Throws() {
        runWithScenario((contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            final Promisor<Integer> promisor = new Promisor<>() {
                @Override
                public Integer demand() {
                    return 0;
                }
                
                @Override
                public int decrementUsage() {
                    //noinspection resource
                    contracts.bind(contract, this);
                    return 1;
                }
            };
            
            ignore(contracts.bind(contract, promisor));
            
            assertThrown(ContractException.class, () -> implicitClose(closeContracts));
        });
    }
    
    @Test
    default void contracts_InternalCoverage() {
        assertInstantiateThrows(ContractsTestsTools.class);
    }
    
    final class ContractsTestsTools {
        private ContractsTestsTools() {
            throw new AssertionError("Illegal constructor");
        }
        
        interface ScenarioConfig extends BiConsumer<Contracts, AutoClose> {
        
        }
        
        static void runWithScenario(ScenarioConfig config) {
            final Contracts contracts = GlobalContracts.createContracts(new Contracts.Config() {});
            try (AutoClose autoClose = contracts.open()) {
                config.accept(contracts, autoClose);
            }
        }
    }
}
