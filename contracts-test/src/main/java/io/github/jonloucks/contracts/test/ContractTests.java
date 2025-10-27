package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contract;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;

public interface ContractTests {

    @Test
    default void contract_Instantiate_Throws() {
        assertInstantiateThrows(Contract.class);
    }
    
    @Test
    default void contract_create_withNullTypes_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Contract.create("test", (String[]) null));
    }
    
    @Test
    default void contract_create_withNullConfig_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Contract.create((Contract.Config<?>)null));
    }
    
    @Test
    default void contract_create_withNullClass_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Contract.create((Class<?>)null));
    }
    
    @Test
    default void contract_create_withNullClassAndBuilder_Throws() {
        final Consumer<Contract.Config.Builder<String>> builderConsumer = b -> {};
        assertThrown(IllegalArgumentException.class, () -> Contract.create(null, builderConsumer));
    }
    
    @Test
    default void contract_create_withClassAndNullBuilder_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Contract.create(String.class, null));
    }
    
    @Test
    default void contract_create_withClassAndBuilder_Works() {
        final Contract<String> contract = Contract.create(String.class,
            b -> {
                assertSame(b, b.name("test"));
                assertSame(b, b.replaceable(true));
                assertSame(b, b.typeName("chars"));
            });
        
        assertObject(contract);
        assertEquals("test", contract.getName());
        assertEquals("chars", contract.getTypeName());
        assertTrue(contract.isReplaceable(), "Contract should have been replaceable");
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
    default void contract_create_ByClass_Works() {
        final Class<String> contractClass = String.class;
        final Contract<String> contract = Contract.create(contractClass);
        final Contract.Config<String> expectedConfig = new Contract.Config<>() {
            
            @Override
            public String name() {
                return contractClass.getTypeName();
            }
            
            @Override
            public String typeName() {
                return contractClass.getTypeName();
            }
            
            @Override
            public String cast(Object instance) {
                return contractClass.cast(instance);
            }
            
        };
        assertContract(contract, expectedConfig, "hello");
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
            () -> assertThrows(ClassCastException.class, () -> defaults.cast(12L), "Cast should fail on wrong type")
        );
    }
}
