package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Promisor;
import io.github.jonloucks.contracts.api.Promisors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.contracts.test.Tools.withContracts;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ExtractorPromisorTests {
    
    @Test
    default void extractPromisor_NullReferent_Throws() {
        withContracts(contracts -> {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                promisors.createExtractPromisor(null, t -> "xyz")
            );
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void extractPromisor_NullRecast_Throws() {
        withContracts(contracts -> {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                promisors.createExtractPromisor(() -> "abc", null)
            );
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void extractPromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        withContracts(contracts -> {
            final int usages = 5;
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
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
                () -> verify(deliverable, never()).open(),
                () -> verify(deliverable, never()).close()
            );
        });
    }
}
