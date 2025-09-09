package io.github.jonloucks.contracts.test;

import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("CodeBlock2Expr")
public interface ToolsTests {
    
    @Test
    default void tools_Instantiate_Throws() {
        assertInstantiateThrows(Tools.class);
    }
    
    @Test
    default void tools_assertInstantiateThrows_WithPublic_Throws() {
        final AssertionError thrown = assertThrows(AssertionError.class, ()-> {
            assertInstantiateThrows(String.class);
        });
        assertThrown(thrown);
    }
}
