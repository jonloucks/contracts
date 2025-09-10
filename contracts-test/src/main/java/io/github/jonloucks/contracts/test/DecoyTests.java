package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Service;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("Convert2MethodRef")
public interface DecoyTests {
    @Test
    default void decoy_Defaults() {
        final Contract<String> contract = Contract.create("testContract");
        final Decoy<String> decoy = new Decoy<>() {};
        
        assertAll(
            () -> assertNull(decoy.demand(), "demand should return null."),
            () -> assertNull(decoy.claim(contract), "claim should return null."),
            () -> assertDoesNotThrow(() -> decoy.startup(), "startup should throw an exception."),
            () -> assertDoesNotThrow(() -> decoy.shutdown(), "shutdown should throw an exception."),
            () -> assertEquals(1, decoy.incrementUsage(), "incrementUsage should return 1."),
            () -> assertEquals(1, decoy.decrementUsage(), "decrementUsage should return 1."),
            () -> assertFalse(decoy.isBound(contract), "isBound should return false."),
            () -> assertNotNull(decoy.bind(contract, ()->"hello")),
            () -> assertThrows(Exception.class, () -> decoy.createService(new Service.Config(){}))
        );
    }
}
