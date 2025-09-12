package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
public interface ContractsTests {
    
    @Test
    default void contracts_Instantiate_Throws() {
        assertInstantiateThrows(Contracts.class);
    }
    
    @Test
    default void contracts_claimContract_WithNullContract_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->  {
            Contracts.claimContract(null);
        });

        assertThrown(thrown);
    }
    
    @Test
    default void contracts_claimContract_WithUnknownContract_Throws() {
        final Contract<String> contract = Contract.create("testContract");
        final ContractException thrown = assertThrows(ContractException.class, () ->  {
            Contracts.claimContract(contract);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void contracts_claimContract_WithPromisedContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        final Shutdown releaseBinding = Contracts.bindContract(contract, () -> "abc");
        try {
            assertAll(
                () -> assertNotNull(releaseBinding),
                () -> assertSame("abc", Contracts.claimContract(contract)));
        } finally {
            releaseBinding.shutdown();
        }
    }
    
    @Test
    default void contracts_isContractBound_WithUnboundContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        
        assertFalse(Contracts.isContractBound(contract), "Unbound Contract was bound.");
    }
    
    @Test
    default void contracts_isContractBound_WithBoundContract_Works() {
        final Contract<String> contract = Contract.create("testContract");
        final Shutdown releaseBinding = Contracts.bindContract(contract, () -> "abc");
        
        try {
            assertTrue(Contracts.isContractBound(contract), "Unbound Contract was bound.");
        } finally {
            releaseBinding.shutdown();
        }
    }
}
