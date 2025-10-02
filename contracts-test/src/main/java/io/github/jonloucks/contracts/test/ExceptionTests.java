package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.ContractException;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertIsSerializable;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ThrowableNotThrown", "CodeBlock2Expr"})
public interface ExceptionTests {
    
    @Test
    default void exception_ContractException_WithNullMessage_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new ContractException(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void exception_ContractException_IsSerializable() {
        assertIsSerializable(ContractException.class);
    }
    
    @Test
    default void exception_ContractException_WithValid_Works() {
        final ContractException exception = new ContractException("abc");
        
        assertThrown(exception, "abc");
    }
    
    @Test
    default void exception_ContractException_WithCause_Works() {
        final OutOfMemoryError cause = new OutOfMemoryError("memory");
        final ContractException exception = new ContractException("abc", cause);
        
        assertThrown(exception, cause, "abc");
    }
}
