package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static io.github.jonloucks.contracts.test.Tools.withContracts;
import static org.junit.jupiter.api.Assertions.*;

public interface PromisorsTests extends
    ValuePromisorTests,
    ExtractorPromisorTests,
    SingletonPromisorTests,
    LifeCyclePromisorTests {
 
    @Test
    default void promisors_getContractDeliverable() {
        withContracts(contracts -> {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            
            assertNotNull(promisors);
            assertObject(promisors);
        });
    }
}
