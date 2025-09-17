package io.github.jonloucks.contracts.test.run;

import io.github.jonloucks.contracts.Stub;
import org.junit.jupiter.api.Test;

public interface Tests {
    
    @Test
    default void contractsAll_test() {
        new Stub();
    }
}
