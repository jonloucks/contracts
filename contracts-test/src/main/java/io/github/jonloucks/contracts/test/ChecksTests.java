package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Checks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.*;


@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
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
        
        assertThrown(thrown, "xyz");
    }
}
