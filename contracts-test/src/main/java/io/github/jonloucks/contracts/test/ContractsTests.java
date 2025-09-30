package io.github.jonloucks.contracts.test;


import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.function.BiConsumer;

import static io.github.jonloucks.contracts.test.ContractsTests.ContractsTestsTools.runWithScenario;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.contracts.test.Tools.createReplaceableContract;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"CodeBlock2Expr", "Convert2MethodRef"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ContractsTests {
    
    @Test
    default void contracts_claim_NotBound_Throws() {
        runWithScenario( (contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            
            final ContractException thrown = assertThrows(ContractException.class, () -> {
                contracts.claim(contract);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void contracts_replaceBinding_Works() {
        runWithScenario( (contracts, closeContracts) -> {
            final Contract<Integer> contract = createReplaceableContract(Integer.class);

            try (AutoClose firstBinding = contracts.bind(contract, ()-> 9)) {
                try (AutoClose secondBinding = contracts.bind(contract, ()-> 100)) {
                    final AutoClose ignored = secondBinding;
                    firstBinding.close();
                    assertEquals(100, contracts.claim(contract));
                }
            }
        });
    }
    
    @Test
    default void contracts_idempotent_DoesNotThrow() {
        runWithScenario( (contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            
            try (AutoClose closeBinding = contracts.bind(contract, ()-> 9)) {
                assertDoesNotThrow(() -> {
                    closeBinding.close();
                    closeBinding.close();
                });
            }
        });
    }
    
    @Test
    default void contracts_open_twice_IsIgnored() {
        runWithScenario( (contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            
            try (AutoClose closeBinding = contracts.bind(contract, ()-> 3)) {
                final AutoClose ignored = closeBinding;
                
                contracts.open().close();
                
                assertEquals(3, contracts.claim(contract));
            }
        });
    }
    
    @Test
    default void contracts_bind_AlreadyBoundAndNotReplaceable_Throws() {
        runWithScenario( (contracts, closeContracts) -> {
            final Contract<Integer> contract = Contract.create("test");
            try (AutoClose closeBinding = contracts.bind(contract, ()-> 3)) {
                final AutoClose ignored = closeBinding;
                
                final ContractException thrown = assertThrows(ContractException.class, () -> {
                    //noinspection resource
                    contracts.bind(contract, ()-> 4);
                });
                
                assertThrown(thrown);
            }
        });
    }
    
    @Test
    default void contracts_bind_Twice_Throws() {
        runWithScenario( (contracts, closeContracts) -> {
            final Contract<Integer> contract = createReplaceableContract(Integer.class);
            final Promisor<Integer> promisor = () -> 7;
            try (AutoClose closeBinding = contracts.bind(contract, promisor)) {
                final AutoClose ignored = closeBinding;
                
                final ContractException thrown = assertThrows(ContractException.class, () -> {
                    //noinspection resource
                    contracts.bind(contract, promisor);
                });
                
                assertThrown(thrown);
            }
        });
    }
    
    @Test
    default void contracts_BindOnClose_Throws() {
        runWithScenario( (contracts, closeContracts) -> {
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
            
            //noinspection resource
            contracts.bind(contract, promisor);
            
            final ContractException thrown = assertThrows(ContractException.class, () -> {
                closeContracts.close();
            });
            
            assertThrown(thrown);
        });
    }
    
    final class ContractsTestsTools {
        interface ScenarioConfig extends BiConsumer<Contracts, AutoClose> {
        
        }
        
        static void runWithScenario(ScenarioConfig config) {
            final Contracts contracts = GlobalContracts.createContracts(new Contracts.Config() {});
            try (AutoClose autoClose = contracts.open()){
                config.accept(contracts, autoClose);
            }
        }
    }
}
