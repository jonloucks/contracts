package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.Promisor;
import io.github.jonloucks.contracts.api.Promisors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public interface SingletonPromisorTests {
    
    @Test
    default void createSingletonPromisor_WithNullReferent_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            promisors.createSingletonPromisor(null)
        );
        
        assertThrown(thrown);
    }
    
    @Test
    default void createSingletonPromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        final int usages = 5;
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        when(referent.demand()).thenReturn(deliverable);
        final Promisor<Decoy<Integer>> promisor = promisors.createSingletonPromisor(referent);
        
        assertNotNull(promisor, "should not return null.");
        
        for (int i = 0; i < usages; i++) {
            promisor.incrementUsage();
        }
        final Decoy<Integer> delivery1 = promisor.demand();
        final Decoy<Integer> delivery2 = promisor.demand();
        
        for (int i = 0; i < usages; i++) {
            promisor.decrementUsage();
        }
        assertAll(
            () -> assertObject(promisor),
            () -> assertSame(deliverable, delivery1, "first deliverable should match."),
            () -> assertSame(deliverable, delivery2,"second deliverable should match."),
            () -> verify(referent, times(1)).demand(),
            () -> verify(referent, times(usages)).decrementUsage(),
            () -> verify(referent, times(usages)).incrementUsage(),
            () -> verify(deliverable, never()).incrementUsage(),
            () -> verify(deliverable, never()).decrementUsage(),
            () -> verify(deliverable, never()).startup(),
            () -> verify(deliverable, never()).shutdown()
        );
    }
}
