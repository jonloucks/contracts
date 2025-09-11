package io.github.jonloucks.contracts.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("CodeBlock2Expr")
public interface ToolsTests {
    
    @Test
    default void tools_Instantiate_Throws() {
        assertInstantiateThrows(Tools.class);
    }
    
    @Test
    default void tools_assertInstantiate_WithPublic_Throws() {
        final AssertionError thrown = assertThrows(AssertionError.class, ()-> {
            assertInstantiateThrows(String.class);
        });
        assertThrown(thrown);
    }
    
    @Test
    default void tools_sleep_WithNullDuration_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, ()-> {
            Tools.sleep(null);
        });
        assertThrown(thrown);
    }
    
    @ParameterizedTest(name = "Duration {0} milliseconds")
    @ValueSource(ints = {0, 5, 10, 100, 200})
    default void tools_sleep_WithDuration(long milliseconds) {
        final Duration expectedDuration = Duration.ofMillis(milliseconds);
        final Duration allowedDifference = Duration.ofMillis(10);
        final Instant start = Instant.now();
        
        Tools.sleep(expectedDuration);
        
        final Duration actualDuration = Duration.between(start, Instant.now());
        
        assertTrue(actualDuration.compareTo(expectedDuration) >= 0);
        assertTrue(actualDuration.minus(expectedDuration).compareTo(allowedDifference) <= 0);
    }
    
    @ParameterizedTest(name = "Duration {0} milliseconds")
    @ValueSource(ints = {-1, -2 })
    default void tools_sleep_WithInvalidDuration(long milliseconds) {
        final Duration expectedDuration = Duration.ofMillis(milliseconds);
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Tools.sleep(expectedDuration);
        });
        assertThrown(thrown);
    }
}
