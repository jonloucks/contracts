package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
public interface ToolsTests {
    
    @Test
    default void tools_Instantiate_Throws() {
        assertInstantiateThrows(Tools.class);
    }
    
    @Test
    default void tools_sanitize_WithNullEllipses_DoesNotThrow() {
        assertDoesNotThrow(() -> sanitize((Executable[])null));
    }
    
    @Test
    default void tools_sanitize_WithNullElement_DoesNotThrow() {
        assertDoesNotThrow(() -> sanitize((Executable)null ));
    }
    
    @Test
    default void tools_sanitize_WithSingeSanitize_Works(@Mock Executable sanitizer) throws Throwable {
        assertDoesNotThrow(() -> sanitize(sanitizer));
        
        verify(sanitizer, times(1)).execute();
    }
    
    @Test
    default void tools_sanitize_WithMany_Works(@Mock Executable sanitizer) throws Throwable {
        final Executable throwingSanitizer = () -> { throw  new IllegalStateException("Oh My"); };
        assertDoesNotThrow(() -> sanitize(throwingSanitizer, sanitizer, throwingSanitizer));
        
        verify(sanitizer, times(1)).execute();
    }
    
    @Test
    default void tools_assertContract_Works() {
        final Contract<String> validContract = Contract.create("test");
        final Contract.Config<String> validConfig = String.class::cast;
        
        assertFails(() -> Tools.assertContract(null, validConfig, "abc"));
        assertFails(() -> Tools.assertContract(validContract, null, "abc"));
    }
    
    @Test
    default void tools_assertThrown_Works() {
        final String validReason = "reason";
        final Throwable validCause = new RuntimeException("cause");
        final Throwable validException = new RuntimeException(validReason);
        final Throwable exceptionWithNullReason = new RuntimeException((String)null);
        final Throwable validExceptionWithCause = new RuntimeException(validReason, validCause);
        final Throwable unknownException = new RuntimeException("unknown");
   
        assertAll(
            () -> assertFails(() -> Tools.assertThrown(null, validCause, validReason)),
            
            () -> assertFails(() -> Tools.assertThrown(validCause, null, null)),
            () -> assertFails(() -> Tools.assertThrown(validCause, unknownException, null)),
            () -> assertFails(() -> Tools.assertThrown(validCause, validCause, validReason)),
            
            () -> assertFails(() -> Tools.assertThrown(exceptionWithNullReason, validCause, validReason)),
            () -> assertDoesNotThrow(() -> Tools.assertThrown(validException, null, validReason)),
            () -> assertFails(() -> Tools.assertThrown(validExceptionWithCause, null, validReason)),
            () -> assertFails(() -> Tools.assertThrown(validExceptionWithCause, validCause, "different")),
            () -> assertDoesNotThrow(() -> Tools.assertThrown(validExceptionWithCause, validCause, validReason))
        );
    }
    
    @Test
    default void tools_assertInstantiateThrows_With() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, ()-> {
            assertInstantiateThrows(null);
        });
        assertThrown(thrown);
    }
    
    @Test
    default void tools_assertInstantiate_WithPublic_Fails() {
        assertFails(() -> assertInstantiateThrows(String.class));
    }
    
    @Test
    default void tools_assertObject_WithNull_Fails() {
        assertFails(()->assertObject(null));
    }
    
    @Test
    default void tools_assertObject_WithBadHashCode_Fails() {
        final Object object = new Object() {
            private final AtomicInteger counter = new AtomicInteger();
            @Override
            public int hashCode() {
                return counter.incrementAndGet();
            }
        };
        
        assertFails(()->assertObject(object));
    }
    
    @Test
    default void tools_assertObject_WithNullToString_Fails() {
        final Object object = new Object() {
            @Override
            public String toString() {
                return null;
            }
        };
        assertFails(()->assertObject(object));
    }
    
    @Test
    default void tools_assertObject_BadEquals_Fails() {
        final Object object = new Object() {
            @SuppressWarnings("EqualsDoesntCheckParameterClass")
            @Override
            public boolean equals(Object that) {
                return null == that;
            }
        };
        assertFails(()->assertObject(object));
    }
    
    @Test
    default void tools_assertObject_WithString_Passes() {
        assertDoesNotThrow(() -> {
            assertObject("abc");
        });
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
