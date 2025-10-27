package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Checks;
import io.github.jonloucks.contracts.api.Contract;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;

public interface ChecksTests {
    
    @Test
    default void checks_Instantiate_Throws() {
        assertInstantiateThrows(Checks.class);
    }
    
    @Test
    default void checks_contractCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.contractCheck(null));
    }
    
    @Test
    default void checks_contractCheck_WithValid_Works() {
        final Contract<?> contract = Contract.create(Instant.class);
        
        assertSame(contract, Checks.contractCheck(contract));
    }
    
    @Test
    default void checks_contractsCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.contractsCheck(null));
    }
    
    @Test
    default void checks_contractsCheck_WithValid_Works() {
        final Contract<?> contract = Contract.create(Instant.class);
        
        assertSame(contract, Checks.contractCheck(contract));
    }
    
    @Test
    default void checks_configCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.configCheck(null));
    }
    
    @Test
    default void checks_configCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.configCheck(config));
    }
    
    @Test
    default void checks_nameCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.nameCheck(null));
    }
    
    @Test
    default void checks_nameCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.nameCheck(config));
    }
    
    @Test
    default void checks_builderConsumerCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.builderConsumerCheck(null));
    }
    
    @Test
    default void checks_builderConsumerCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.builderConsumerCheck(config));
    }
    
    @Test
    default void checks_builderCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.builderCheck(null));
    }
    
    @Test
    default void checks_builderCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.builderCheck(config));
    }
    
    @Test
    default void checks_typeCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.typeCheck(null));
    }
    
    @Test
    default void checks_typeCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.typeCheck(config));
    }
    
    @Test
    default void checks_illegalCheck_WhenFailedIsFalse_Works() {
        final String actual = Checks.illegalCheck("abc", false, "");
        
        assertSame("abc", actual);
    }
    
    @Test
    default void checks_illegalCheck_WhenValid_Works() {
        final String actual = Checks.illegalCheck("abc", false, "");
        
        assertSame("abc", actual);
    }
    
    @Test
    default void checks_illegalCheck_WhenFailed_Throws() {
        //noinspection DataFlowIssue
        assertThrown(IllegalArgumentException.class,
           () -> Checks.illegalCheck("abc", true, "Xyz."), "Xyz.");
    }
    @Test
    default void checks_illegalCheck_WhenNullMessage_Throws() {
        //noinspection DataFlowIssue
        assertThrown(IllegalArgumentException.class, () -> Checks.illegalCheck("abc", false, null));
    }
}
