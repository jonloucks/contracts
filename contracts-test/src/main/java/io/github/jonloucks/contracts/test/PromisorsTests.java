package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static org.junit.jupiter.api.Assertions.*;

public interface PromisorsTests extends
    ValuePromisorTests,
    ExtractorPromisorTests,
    SingletonPromisorTests,
    LifeCyclePromisorTests {
 
    @Test
    default void promisors_getContractDeliverable() {
        final Promisors promisors = GlobalContracts.claimContract(Promisors.CONTRACT);
        
        assertNotNull(promisors);
        assertObject(promisors);
    }
}
