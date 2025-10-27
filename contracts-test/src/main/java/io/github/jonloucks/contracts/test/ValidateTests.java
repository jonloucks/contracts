package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ValidateTests {
    
    @Test
    default void validate_WithNullContracts_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.validateContracts(null));
    }
    
    @Test
    default void validate_WhenBindReturnsNull_Throws(@Mock Contracts contracts) {
        when(contracts.isBound(any())).thenReturn(false);
        assertThrown(ContractException.class, () -> Checks.validateContracts(contracts));
    }
    
    @Test
    default void validate_WithFirstIsBoundIsTrue_Throws(@Mock Contracts contracts) {
        when(contracts.isBound(any())).thenReturn(true);
        assertThrown(ContractException.class,
            () -> Checks.validateContracts(contracts), "Contract should not be bound.");
    }
    
    @Test
    default void validate_bind_ReturnsNull_Throws(@Mock Contracts contracts) {
        when(contracts.isBound(any())).thenReturn(false);
        when(contracts.bind(any(), any())).thenAnswer((Answer<AutoClose>) invocationOnMock -> {
            return null;
        });
        assertThrown(ContractException.class,
            () -> Checks.validateContracts(contracts), "Contract bind returned null.");
    }
    
    @Test
    default void validate_isBound_AfterBind_ReturnsFalse_Throws(@Mock Contracts contracts, @Mock AutoClose closeBinding) {
        when(contracts.isBound(any())).thenReturn(false);
        when(contracts.bind(any(), any())).thenAnswer((Answer<AutoClose>) invocationOnMock -> {
            when(contracts.isBound(any())).thenReturn(false);
            return closeBinding;
        });
        assertThrown(ContractException.class,
            () -> Checks.validateContracts(contracts), "Contract should have been bound.");
    }
    
    @Test
    default void validate_claim_AfterBind_ReturnsUnexpected_Throws(@Mock Contracts contracts, @Mock AutoClose closeBinding) {
        when(contracts.isBound(any())).thenReturn(false);
        when(contracts.bind(any(), any())).thenAnswer((Answer<AutoClose>) invocationOnMock -> {
            when(contracts.isBound(any())).thenReturn(true);
            return closeBinding;
        });
        doAnswer((Answer<Void>) invocation -> {
            when(contracts.isBound(any())).thenReturn(false);
            return null;
        }).when(closeBinding).close();
        when(contracts.claim(any())).thenAnswer((Answer<?>) invocationOnMock -> {
            return null;
        });
        
        assertThrown(ContractException.class, () -> Checks.validateContracts(contracts), "Contract claiming not working.");
    }
    
    @Test
    default void validate_claim_AfterBind_ThrowsUnexpected_Throws(@Mock Contracts contracts, @Mock AutoClose closeBinding) {
        when(contracts.isBound(any())).thenReturn(false);
        when(contracts.bind(any(), any())).thenAnswer((Answer<AutoClose>) invocationOnMock -> {
            when(contracts.isBound(any())).thenReturn(true);
            return closeBinding;
        });
        doAnswer((Answer<Void>) invocation -> {
            when(contracts.isBound(any())).thenReturn(false);
            return null;
        }).when(closeBinding).close();
        when(contracts.claim(any())).thenAnswer((Answer<?>) invocationOnMock -> {
            throw new ArithmeticException("Math overflow.");
        });
        
        assertThrown(ContractException.class, () -> Checks.validateContracts(contracts), "Contracts unexpected validation error.");
    }
    
    @Test
    default void validate_Success_DoesNotThrow(@Mock Contracts contracts, @Mock AutoClose closeBinding) {
        final AtomicReference<Promisor<?>> promisor = new AtomicReference<>();
        when(contracts.isBound(any())).thenReturn(false);
        when(contracts.bind(any(), any())).thenAnswer((Answer<AutoClose>) invocationOnMock -> {
            promisor.set(invocationOnMock.getArgument(1));
            when(contracts.isBound(any())).thenReturn(true);
            return closeBinding;
        });
        doAnswer((Answer<Void>) invocation -> {
            when(contracts.isBound(any())).thenReturn(false);
            return null;
        }).when(closeBinding).close();
        when(contracts.claim(any())).thenAnswer((Answer<?>) invocationOnMock -> {
            return promisor.get().demand();
        });
        
        assertDoesNotThrow(() -> {
            Checks.validateContracts(contracts);
        });
    }
    
    @Test
    default void validate_AfterUnbindContractIsStillBound_Throws(@Mock Contracts contracts, @Mock AutoClose closeBinding) {
        final AtomicReference<Promisor<?>> promisor = new AtomicReference<>();
        when(contracts.isBound(any())).thenReturn(false);
        doAnswer((Answer<Void>) invocation -> {
            when(contracts.isBound(any())).thenReturn(true);
            return null;
        }).when(closeBinding).close();
        
        when(contracts.bind(any(), any())).thenAnswer((Answer<AutoClose>) onMock -> {
            promisor.set(onMock.getArgument(1));
            when(contracts.isBound(any())).thenReturn(true);
            return closeBinding;
        });
        when(contracts.claim(any())).thenAnswer((Answer<?>) invocationOnMock -> {
            return promisor.get().demand();
        });
        assertThrown(ContractException.class, () -> Checks.validateContracts(contracts), "Contract unbinding not working.");
    }
}
