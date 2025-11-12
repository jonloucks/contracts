package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.*;

public interface AutoTests {
    
    @Test
    default void auto_AutoOpen_NONE() {
        assertObject(AutoOpen.NONE);
        
        try (AutoClose autoClose = AutoOpen.NONE.open()) {
            assertIdempotent(autoClose);
        }
    }
    
    @Test
    default void auto_AutoClose_NONE() {
        assertObject(AutoClose.NONE);
        assertIdempotent(AutoClose.NONE);
    }
}
