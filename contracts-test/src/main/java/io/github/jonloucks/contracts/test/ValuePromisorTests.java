package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ValuePromisorTests {
    
    @Test
    default void createValuePromisor_WithNullValue_Works() {
        final Promisors promisors = GlobalContracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createValuePromisor(null);
        
        assertNotNull(promisor, "should not return null.");
        assertNull(promisor.demand(), "promisor demand should be null.");
    }
    
    @Test
    default void createValuePromisor_WithSimpleValue_Works() {
        final Promisors promisors = GlobalContracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createValuePromisor("abc");
        
        assertNotNull(promisor, "should not return null.");
        assertObject(promisor);
        assertSame("abc", promisor.demand(), "deliverables should match.");
    }
    
    @Test
    default void createValuePromisor_incrementUsage(@Mock AutoOpen deliverable) {
        final Promisors promisors = GlobalContracts.claimContract(Promisors.CONTRACT);
        final Promisor<AutoOpen> promisor = promisors.createValuePromisor(deliverable);
        
        assertNotNull(promisor, "should not return null.");
        
        promisor.incrementUsage();
        
        //noinspection resource,LambdaBodyCanBeCodeBlock
        assertAll(
            () -> assertSame(deliverable, promisor.demand(), "deliverables should match."),
            () -> verify(deliverable, never()).open()
        );
    }
    
    @Test
    default void createValuePromisor_decrementUsage(@Mock AutoClose deliverable) {
        final Promisors promisors = GlobalContracts.claimContract(Promisors.CONTRACT);
        final Promisor<AutoClose> promisor = promisors.createValuePromisor(deliverable);
        
        assertNotNull(promisor, "should not return null.");
        
        promisor.decrementUsage();
        
        assertAll(
            () -> assertSame(deliverable, promisor.demand(), "deliverables should match."),
            () -> verify(deliverable, never()).close()
        );
    }
}
