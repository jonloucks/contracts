package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.ContractException;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertIsSerializable;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;

@SuppressWarnings({"ThrowableNotThrown"})
public interface ExceptionTests {
    
    @Test
    default void exception_ContractException_WithNullMessage_Throws() {
        assertThrown(IllegalArgumentException.class, () -> new ContractException(null));
    }
    
    @Test
    default void exception_ContractException_IsSerializable() {
        assertIsSerializable(ContractException.class);
    }
    
    @Test
    default void exception_ContractException_WithValid_Works() {
        final ContractException exception = new ContractException("Abc.");
        
        assertThrown(exception, "Abc.");
    }
    
    @Test
    default void exception_ContractException_WithCause_Works() {
        final OutOfMemoryError cause = new OutOfMemoryError("Out of memory.");
        final ContractException exception = new ContractException("Abc.", cause);
        
        assertThrown(exception, cause, "Abc.");
    }
}
