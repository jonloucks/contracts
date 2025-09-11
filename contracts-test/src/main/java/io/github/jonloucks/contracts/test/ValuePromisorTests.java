package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.Promisor;
import io.github.jonloucks.contracts.api.Promisors;
import io.github.jonloucks.contracts.api.Shutdown;
import io.github.jonloucks.contracts.api.Startup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public interface ValuePromisorTests {
    
    @Test
    default void createValuePromisor_WithNullValue_Works() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createValuePromisor(null);
        
        assertNotNull(promisor, "should not return null.");
        assertNull(promisor.demand(), "promisor demand should be null.");
    }
    
    @Test
    default void createValuePromisor_WithSimpleValue_Works() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createValuePromisor("abc");
        
        assertNotNull(promisor, "should not return null.");
        assertObject(promisor);
        assertSame("abc", promisor.demand(), "deliverables should match.");
    }
    
    @Test
    default void createValuePromisor_incrementUsage(@Mock Startup deliverable) {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<Startup> promisor = promisors.createValuePromisor(deliverable);
        
        assertNotNull(promisor, "should not return null.");
        
        promisor.incrementUsage();
        
        assertAll(
            () -> assertSame(deliverable, promisor.demand(), "deliverables should match."),
            () -> verify(deliverable, never()).startup()
        );
    }
    
    @Test
    default void createValuePromisor_decrementUsage(@Mock Shutdown deliverable) {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<Shutdown> promisor = promisors.createValuePromisor(deliverable);
        
        assertNotNull(promisor, "should not return null.");
        
        promisor.decrementUsage();
        
        assertAll(
            () -> assertSame(deliverable, promisor.demand(), "deliverables should match."),
            () -> verify(deliverable, never()).shutdown()
        );
    }
}
