package io.github.jonloucks.contracts.smoke.test;

import io.github.jonloucks.contracts.smoke.Main;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public interface SmokeTests {
    
    @Test
    default void main_Instantiate_Main_Throws() {
        assertInstantiateThrows(Main.class);
    }
    
    @Test
    default void smoke_main() {
        final AtomicInteger code = new AtomicInteger(Integer.MIN_VALUE/2);
        final IntConsumer savedSystemExit = Main.setSystemExit(code::set);
        try {
            Main.main(new String[]{});
            assertEquals(0, code.get());
        } finally {
            Main.setSystemExit(savedSystemExit);
        }
    }
    
    @Test
    default void smoke_main_Failure() {
        final AtomicInteger code = new AtomicInteger(Integer.MIN_VALUE/2);
        final IntConsumer savedSystemExit = Main.setSystemExit(i -> {
            code.set(i);
            if (i == 0) {
                // introducing exception when smoke test thinks it was successful
                throw new RuntimeException("Pretend failure.");
            }
        });
        try {
            Main.main(new String[]{});
            assertEquals(1, code.get());
        } finally {
            Main.setSystemExit(savedSystemExit);
        }
    }
}
