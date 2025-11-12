package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    default void tools_assertFails_WhenNoFailure_Fails() {
        assertFails( // Note: Using assertFails to test assertFails
            () -> assertFails(()->{}));
    }
    
    @Test
    default void tools_assertFails_WhenThrowsNonAssertions_Fails() {
        assertFails( // Note: Using assertFails to test assertFails
            () -> assertFails(() -> { throw new IllegalStateException("Oh My."); }));
    }
    
    @Test
    default void tools_assertFails_WhenThrowsAsserts_Succeeds() {
       assertDoesNotThrow(() -> assertFails(()->{ throw new AssertionError("Oh My."); }));
    }
    
    @Test
    default void tools_assertContract_Works() {
        final Contract<String> validContract = Contract.create("test");
        final Contract.Config<String> validConfig = String.class::cast;
        
        assertFails(() -> assertContract(null, validConfig, "abc"));
        assertFails(() -> assertContract(validContract, null, "abc"));
    }
    
    @Test
    default void tools_assertThrown_Works() {
        final String validReason = "This is a reason.";
        final Throwable validCause = new RuntimeException("This is a cause.");
        final Throwable validException = new RuntimeException(validReason);
        final Throwable exceptionWithNullReason = new RuntimeException((String)null);
        final Throwable validExceptionWithCause = new RuntimeException(validReason, validCause);
        final Throwable unknownException = new RuntimeException("This is an unknown exception.");
   
        assertAll(
            () -> assertFails(() -> assertThrown(null, validCause, validReason)),
            () -> assertFails(() -> assertThrown(validCause, null, null)),
            () -> assertFails(() -> assertThrown(validCause, unknownException, null)),
            () -> assertFails(() -> assertThrown(validCause, validCause, validReason)),
            () -> assertFails(() -> assertThrown(exceptionWithNullReason, validCause, validReason)),
            () -> assertDoesNotThrow(() -> assertThrown(validException, null, validReason)),
            () -> assertFails(() -> assertThrown(validExceptionWithCause, null, validReason)),
            () -> assertFails(() -> assertThrown(validExceptionWithCause, validCause, "Different.")),
            () -> assertDoesNotThrow(() -> assertThrown(validExceptionWithCause, validCause, validReason))
        );
    }
    
    @Test
    default void tools_assertThrown_WithThrowing_Works() {
        final String validReason = "This is a reason.";
        final Throwable validCause = new RuntimeException("This is a cause.");
        final Throwable validException = new RuntimeException(validReason);
        final Throwable exceptionWithNullReason = new RuntimeException((String)null);
        final Throwable validExceptionWithCause = new RuntimeException(validReason, validCause);
        final Throwable unknownException = new RuntimeException("This is an unknown exception.");
        
        assertAll(
            () -> assertFails(() -> assertThrown(null, validCause)),
            () -> assertFails(() -> assertThrown(validCause, unknownException)),
            () -> assertFails(() -> assertThrown(validCause, validCause)),
            () -> assertFails(() -> assertThrown(exceptionWithNullReason, validCause)),
            () -> assertDoesNotThrow(() -> assertThrown(validException, (Throwable)null)),
            () -> assertFails(() -> assertThrown(validExceptionWithCause, (Throwable)null)),
            () -> assertDoesNotThrow(() -> assertThrown(validExceptionWithCause, validCause))
        );
    }
    
    @Test
    default void tools_assertInstantiateThrows_With() {
        assertThrown(IllegalArgumentException.class,
            ()-> assertInstantiateThrows(null));
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
            @Override
            public int hashCode() {
                return 1;
            }
        };
        assertFails(()->assertObject(object));
    }
    
    @Test
    default void tools_assertObject_WithString_Passes() {
        assertDoesNotThrow(() -> assertObject("abc") );
    }
    
    @Test
    default void tools_assertMayThrow_WithNullType_Throws() {
        final Executable executable = () -> {};
        assertThrown(IllegalArgumentException.class,
            () -> assertMayThrow(null, executable ));
    }
    
    @Test
    default void tools_assertMayThrow_WithNullExecutable_Throws() {
        final Class<IllegalStateException> type = IllegalStateException.class;
        assertThrown(IllegalArgumentException.class,
            () -> assertMayThrow(type, null));
    }
    
    @Test
    default void tools_assertMayThrow_WithAllowedThrow_DoesNotThrow() {
        final Class<IllegalArgumentException> type = IllegalArgumentException.class;
        final IllegalArgumentException expected = new IllegalArgumentException("Illegal.");
        final Executable executable = () -> { throw expected; };
        
        assertDoesNotThrow(() -> assertMayThrow(type, executable));
    }
    
    @Test
    default void tools_assertMayThrow_WithDisallowedThrow_Fails() {
        final Class<IllegalStateException> type = IllegalStateException.class;
        final Executable executable = () -> { throw new IOException("Input."); };
        
        assertFails(() -> assertMayThrow(type, executable));
    }
    
    @Test
    default void tools_assertThrownType_WithNullType_Throws() {
        final IllegalArgumentException expected = new IllegalArgumentException("Illegal.");
        
        assertThrown(IllegalArgumentException.class,
            () -> assertThrownType(null, expected, "Problem."));
    }
    
    @Test
    default void tools_assertThrownType_WithNullThrown_Throws() {
        final Class<IllegalStateException> type = IllegalStateException.class;
        assertThrown(IllegalArgumentException.class,
            () -> assertThrownType(type, null, "Problem."));
    }
    
    @Test
    default void tools_assertThrownType_WithAllowedThrow_DoesNotThrow() {
        final Class<IllegalArgumentException> type = IllegalArgumentException.class;
        final IllegalArgumentException expected = new IllegalArgumentException("Illegal.");
  
        assertDoesNotThrow(() -> assertThrownType(type, expected, "Problem."));
    }
    
    @Test
    default void tools_assertThrownType_WithDisallowedThrow_Fails() {
        final Class<IllegalStateException> type = IllegalStateException.class;
        final IOException unexpected = new IOException("Out of diskspace.");
        
        assertFails(() -> assertThrownType(type, unexpected, "Problem."));
    }

    @Test
    default void tools_sleep_WithNullDuration_Throws() {
        assertThrown(IllegalArgumentException.class,
            () -> sleep(null),"Duration must be present.");
    }
    
    @ParameterizedTest(name = "Duration {0} milliseconds")
    @ValueSource(ints = {0, 5, 10, 100, 200})
    default void tools_sleep_WithDuration(long milliseconds) {
        final Duration expectedDuration = Duration.ofMillis(milliseconds);
        final Duration allowedDifference = Duration.ofMillis(100);
        final Instant start = Instant.now();
        
        sleep(expectedDuration);
        
        final Duration actualDuration = Duration.between(start, Instant.now());
        
        assertTrue(actualDuration.compareTo(expectedDuration) >= 0);
        assertTrue(actualDuration.minus(expectedDuration).compareTo(allowedDifference) <= 0);
    }
    
    @ParameterizedTest(name = "Duration {0} milliseconds")
    @ValueSource(ints = {-1, -2 })
    default void tools_sleep_WithInvalidDuration(long milliseconds) {
        final Duration expectedDuration = Duration.ofMillis(milliseconds);
        assertThrown(IllegalArgumentException.class,
            () -> sleep(expectedDuration),
            "Duration must not be negative.");
    }
    
    @Test
    default void tools_assertIdempotent_WithNullAutoClose_Throws() {
        assertThrown(IllegalArgumentException.class,
            () -> assertIdempotent(null),
            "AutoClose must be present.");
    }
    
    @Test
    default void tools_assertIdempotent_WhenFirstCloseThrows_Fails() {
        final AutoClose autoClose = () -> { throw new RuntimeException("Not idempotent."); };
        assertFails(() -> assertIdempotent(autoClose));
    }
    
    @Test
    default void tools_assertIdempotent_WhenSecondCloseThrows_Fails() {
        final AtomicInteger counter = new AtomicInteger();
        final AutoClose autoClose = () -> {
            if (counter.getAndIncrement() == 2) {
                throw new RuntimeException("Not idempotent.");
            }
        };
        assertFails(() -> assertIdempotent(autoClose));
    }
    
    @Test
    default void tools_assertIdempotent_WhenThirdCloseThrows_Fails() {
        final AtomicInteger counter = new AtomicInteger();
        final AutoClose autoClose = () -> {
            if (counter.getAndIncrement() == 3) {
                throw new RuntimeException("Not idempotent.");
            }
        };
        assertFails(() -> assertIdempotent(autoClose));
    }
    
    @Test
    default void tools_assertIdempotent_WhenIdempotent_Passes() {
        final AutoClose autoClose = () -> {};
        assertDoesNotThrow(() -> assertIdempotent(autoClose));
    }
}
