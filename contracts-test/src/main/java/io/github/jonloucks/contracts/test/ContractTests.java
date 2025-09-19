package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contract;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertContract;
import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
public interface ContractTests {
    
    @Test
    default void contract_Instantiate_Throws() {
        assertInstantiateThrows(Contract.class);
    }
    
    @Test
    default void contract_create_withNullTypes_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Contract.create("test", (String[]) null);
        });
        
        assertNotNull(thrown.getMessage());
    }
    
    @Test
    default void contract_create_withNullConfig_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Contract.create(null);
        });
        
        assertNotNull(thrown.getMessage());
    }
    
    @Test
    default void contract_create_IntegerContract_Works() {
        final String contractName = "testContract";
        final Contract<Integer> contract = Contract.create(contractName);
        final Contract.Config<Integer> expectedConfig = new Contract.Config<>() {
            
            @Override
            public String name() {
                return contractName;
            }
            
            @Override
            public String typeName() {
                return Integer.class.getTypeName();
            }
            
            @Override
            public Integer cast(Object instance) {
                return (Integer) instance;
            }
            
        };
        assertContract(contract, expectedConfig, 0);
    }
    
    @Test
    default void contract_create_Works() {
        final Contract.Config<String> config = new Contract.Config<>() {
            @Override
            public String cast(Object instance) {
                return (String) instance;
            }
            
            @Override
            public String typeName() {
                return String.class.getName();
            }
            
            @Override
            public String name() {
                return "Some String contract";
            }
            
            @Override
            public boolean isReplaceable() {
                return !Contract.Config.super.isReplaceable();
            }
        };
        final Contract<String> contract = Contract.create(config);
        
        assertContract(contract, config, "abc");
    }
    
    @Test
    default void contract_Config_Defaults() {
        final Contract.Config<String> defaults = x -> (String)x;
        
        assertAll(
            () -> assertFalse(defaults.isReplaceable(), "Default for replaceable."),
            () -> assertEquals("", defaults.name(), "Default for name."),
            () -> assertEquals("", defaults.typeName(), "Default for typeName."),
            () -> assertSame( "abc", defaults.cast("abc"), "Cast should work."),
            () -> assertThrows(ClassCastException.class, () -> defaults.cast(12L))
        );
    }
}
