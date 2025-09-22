package io.github.jonloucks.contracts.runtests;

import io.github.jonloucks.contracts.Stub;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;

public interface Tests {
    
    @Test
    default void stub_Instantiate_Throws() {
        assertInstantiateThrows(Stub.class);
    }
    
    @Test
    default void stub_validate() {
        Stub.validate();
    }
}
