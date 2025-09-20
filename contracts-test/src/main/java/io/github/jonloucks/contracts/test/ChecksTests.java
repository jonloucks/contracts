package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Checks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
    default void checks_contractCheck_WhenNullContract_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.contractCheck(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void checks_illegalCheck_WhenFailedIsFalse_Works() {
        final String actual = Checks.illegalCheck("abc", false, "");
        
        assertSame("abc", actual);
    }
    
    @Test
    default void checks_illegalCheck_WhenNullContract_Throws() {
        final String actual = Checks.illegalCheck("abc", false, "");
        
        assertSame("abc", actual);
    }
    
    @Test
    default void checks_illegalCheck_WhenNull_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Checks.illegalCheck("abc", true, "xyz");
        });
        
        Tools.assertThrown(thrown, "xyz");
    }
}
