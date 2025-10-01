package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Promisor;
import io.github.jonloucks.contracts.api.Promisors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface SingletonPromisorTests {
    
    @Test
    default void createSingletonPromisor_WithNullReferent_Throws() {
        withContracts(contracts -> {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                promisors.createSingletonPromisor(null)
            );
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void createSingletonPromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        withContracts(contracts -> {
            final int usages = 5;
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            when(referent.demand()).thenReturn(deliverable);
            final Promisor<Decoy<Integer>> promisor = promisors.createSingletonPromisor(referent);
            
            assertNotNull(promisor, "should not return null.");
            
            for (int i = 0; i < usages; i++) {
                promisor.incrementUsage();
            }
            @SuppressWarnings("resource") final Decoy<Integer> delivery1 = promisor.demand();
            @SuppressWarnings("resource") final Decoy<Integer> delivery2 = promisor.demand();
            
            for (int i = 0; i < usages; i++) {
                promisor.decrementUsage();
            }
            //noinspection resource
            assertAll(
                () -> assertObject(promisor),
                () -> assertSame(deliverable, delivery1, "first deliverable should match."),
                () -> assertSame(deliverable, delivery2,"second deliverable should match."),
                () -> verify(referent, times(1)).demand(),
                () -> verify(referent, times(usages)).decrementUsage(),
                () -> verify(referent, times(usages)).incrementUsage(),
                () -> verify(deliverable, never()).incrementUsage(),
                () -> verify(deliverable, never()).decrementUsage(),
                () -> verify(deliverable, never()).open(),
                () -> verify(deliverable, never()).close()
            );
        });
    }
}
