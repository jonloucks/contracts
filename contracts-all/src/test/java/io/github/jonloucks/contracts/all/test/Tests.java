package io.github.jonloucks.contracts.all.test;

import io.github.jonloucks.contracts.all.Module;
import org.junit.jupiter.api.Test;

public interface Tests {
    
    @Test
    default void contractsAll_test() {
        new Module();
    }
}
