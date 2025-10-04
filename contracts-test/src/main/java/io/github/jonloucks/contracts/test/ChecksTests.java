package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Checks;
import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Contracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;

import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;


@SuppressWarnings({"CodeBlock2Expr", "DataFlowIssue"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ChecksTests {
    
    @Test
    default void checks_Instantiate_Throws() {
        assertInstantiateThrows(Checks.class);
    }
    
    @Test
    default void checks_contractCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.contractCheck(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void checks_contractCheck_WithValid_Works() {
        final Contract<?> contract = Contract.create(Instant.class);
        
        assertSame(contract, Checks.contractCheck(contract));
    }
    
    @Test
    default void checks_contractsCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.contractsCheck(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void checks_contractsCheck_WithValid_Works(@Mock Contracts contracts) {
        final Contract<?> contract = Contract.create(Instant.class);
        
        assertSame(contract, Checks.contractCheck(contract));
    }
    
    @Test
    default void checks_configCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.configCheck(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void checks_configCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.configCheck(config));
    }
    
    @Test
    default void checks_nameCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.nameCheck(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void checks_nameCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.nameCheck(config));
    }
    
    @Test
    default void checks_builderConsumerCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.builderConsumerCheck(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void checks_builderConsumerCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.builderConsumerCheck(config));
    }
    
    @Test
    default void checks_builderCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.builderCheck(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void checks_builderCheck_WithValid_Works() {
        final Object config = new Object();
        
        assertSame(config, Checks.builderCheck(config));
    }
    
    @Test
    default void checks_typeCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.typeCheck(null);
        });
        
        assertThrown(thrown);
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
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.illegalCheck("abc", true, "Xyz.");
        });
        
        assertThrown(thrown, "Xyz.");
    }
    @Test
    default void checks_illegalCheck_WhenNullMessage_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.illegalCheck("abc", false, null);
        });
        
        assertThrown(thrown);
    }
}
