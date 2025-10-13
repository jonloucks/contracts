package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface AutoTests {
    
    @Test
    default void auto_AutoOpen_NONE() {
        assertObject(AutoOpen.NONE);
        assertDoesNotThrow(() -> AutoOpen.NONE.open().close());
        assertDoesNotThrow(() -> AutoOpen.NONE.open().close());
        assertDoesNotThrow(() -> AutoOpen.NONE.open().close());
    }
    
    @Test
    default void auto_AutoClose_NONE() {
        assertObject(AutoClose.NONE);
        assertDoesNotThrow(AutoClose.NONE::close);
        assertDoesNotThrow(AutoClose.NONE::close);
    }
}
