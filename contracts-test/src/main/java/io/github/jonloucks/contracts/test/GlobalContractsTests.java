package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
public interface GlobalContractsTests {

    @Test
    default void contracts_Instantiate_Throws() {
        assertInstantiateThrows(GlobalContracts.class);
    }
    
    @Test
    default void contracts_getInstance_Works() {
        assertObject(GlobalContracts.getInstance());
    }
    
    @Test
    default void contracts_claimContract_WithNullContract_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->  {
            GlobalContracts.claimContract(null);
        });

        assertThrown(thrown);
    }
    
    @Test
    default void contracts_claimContract_WithUnknownContract_Throws() {
        final Contract<String> contract = Contract.create("testContract");
        final ContractException thrown = assertThrows(ContractException.class, () ->  {
            GlobalContracts.claimContract(contract);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void contracts_claimContract_WithPromisedContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        try (AutoClose releaseBinding = GlobalContracts.bindContract(contract, () -> "abc")){
            assertAll(
                () -> assertNotNull(releaseBinding),
                () -> assertSame("abc", GlobalContracts.claimContract(contract)));
        }
    }
    
    @Test
    default void contracts_isContractBound_WithUnboundContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        
        assertFalse(GlobalContracts.isContractBound(contract), "Unbound Contract was bound.");
    }
    
    @Test
    default void contracts_isContractBound_WithBoundContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        
        try (AutoClose closeBinding = GlobalContracts.bindContract(contract, () -> "abc")){
            final AutoClose ignored = closeBinding;
            assertTrue(GlobalContracts.isContractBound(contract), "Unbound Contract was bound.");
        }
    }
}
