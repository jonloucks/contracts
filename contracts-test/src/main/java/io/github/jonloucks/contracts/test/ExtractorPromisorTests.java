package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.Promisor;
import io.github.jonloucks.contracts.api.Promisors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public interface ExtractorPromisorTests {
    
    @Test
    default void extractPromisor_NullReferent_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            promisors.createExtractPromisor(null, t -> "xyz")
        );
        
        assertThrown(thrown);
    }
    
    @Test
    default void extractPromisor_NullRecast_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            promisors.createExtractPromisor(() -> "abc", null)
        );
        
        assertThrown(thrown);
    }
    
    @Test
    default void extractPromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        final int usages = 5;
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        when(referent.demand()).thenReturn(deliverable);
        final Promisor<String> promisor = promisors.createExtractPromisor(referent, c -> "abc");
        
        assertNotNull(promisor, "should not return null.");
        
        for (int i = 0; i < usages; i++) {
            promisor.incrementUsage();
        }
        for (int i = 0; i < usages; i++) {
            promisor.decrementUsage();
        }
        
        assertAll(
            () -> assertSame("abc", promisor.demand(), "deliverables should match."),
            () -> verify(referent, times(usages)).decrementUsage(),
            () -> verify(referent, times(usages)).incrementUsage(),
            () -> verify(deliverable, never()).incrementUsage(),
            () -> verify(deliverable, never()).decrementUsage(),
            () -> verify(deliverable, never()).startup(),
            () -> verify(deliverable, never()).shutdown()
        );
    }
}
