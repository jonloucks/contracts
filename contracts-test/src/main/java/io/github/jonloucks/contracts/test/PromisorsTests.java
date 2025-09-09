package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
@ExtendWith(MockitoExtension.class)
public interface PromisorsTests {
    @Test
    default void promisors_getContractDeliverable() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        assertNotNull(promisors);
    }
    
    @Test
    default void promisors_createValuePromisor_WithNull_Works() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createValuePromisor(null);
        
        assertNotNull(promisor, "should not return null.");
        assertNull(promisor.demand());
    }
    
    @Test
    default void promisors_createLifeCyclePromisor_WithNull_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            promisors.createLifeCyclePromisor(null)
        );
        
        assertThrown(thrown);
    }
    
    @Test
    default void promisors_LifeCyclePromisor_get_WithUsage_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createLifeCyclePromisor(() -> "abc");
        
        final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            promisor.demand();
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void promisors_createLifeCyclePromisor_WithNullDeliverable_Works() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createLifeCyclePromisor(()->null);
        promisor.incrementUsage();
        
        assertNotNull(promisor, "should not return null.");
        assertNull(promisor.demand());
    }
    
    @Test
    default void promisors_createLifeCyclePromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        final int usages = 5;
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        when(referent.demand()).thenReturn(deliverable);
        final Promisor<Decoy<Integer>> promisor = promisors.createLifeCyclePromisor(referent);
        
        assertNotNull(promisor, "should not return null.");
        
        for (int i = 0; i < usages; i++) {
            promisor.incrementUsage();
        }
        final Decoy<Integer> actual = promisor.demand();
        for (int i = 0; i < usages; i++) {
            promisor.decrementUsage();
        }
        
        assertAll(
            () -> assertSame(deliverable, actual, "deliverables should match."),
            () -> verify(referent, times(usages)).decrementUsage(),
            () -> verify(referent, times(usages)).incrementUsage(),
            () -> verify(deliverable, never()).incrementUsage(),
            () -> verify(deliverable, never()).decrementUsage(),
            () -> verify(deliverable, times(1)).startup(),
            () -> verify(deliverable, times(1)).shutdown()
        );
    }
    
    @Test
    default void promisors_createValuePromisor_WithNonNull_Works() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createValuePromisor("abc");
        
        assertNotNull(promisor, "should not return null.");
        assertSame("abc", promisor.demand(), "deliverables should match.");
    }
    
    @Test
    default void promisors_createValuePromisor_Valid_DoesNotCallStartup(@Mock Startup deliverable) {
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
    default void promisors_createValuePromisor_Valid_DoesNotCallDecrementUsage(@Mock Shutdown deliverable) {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<Shutdown> promisor = promisors.createValuePromisor(deliverable);
        
        assertNotNull(promisor, "should not return null.");
        
        promisor.decrementUsage();
        
        assertAll(
            () -> assertSame(deliverable, promisor.demand(), "deliverables should match."),
            () -> verify(deliverable, never()).shutdown()
        );
    }
    
    @Test
    default void promisors_createDependentPromisor_NullReferent_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            promisors.createDependentPromisor(null, t -> "xyz")
        );
        
        assertThrown(thrown);
    }
    
    @Test
    default void promisors_createTransformPromisor_NullRecast_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            promisors.createDependentPromisor(() -> "abc", null)
        );
        
        assertThrown(thrown);
    }
    
    @Test
    default void promisors_createDependentPromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        final int usages = 5;
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        when(referent.demand()).thenReturn(deliverable);
        final Promisor<String> promisor = promisors.createDependentPromisor(referent, c -> "abc");
        
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
