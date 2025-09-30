package io.github.jonloucks.contracts.impl;

import java.util.concurrent.atomic.AtomicBoolean;

final class IdempotentImpl {
    IdempotentImpl() {
    }
    
    boolean transitionToOpen() {
        return state.compareAndSet(IS_CLOSED, IS_OPEN);
    }
    
    boolean transitionToClosed() {
        return state.compareAndSet(IS_OPEN, IS_CLOSED);
    }
    
    boolean isOpen() {
        return state.get() == IS_OPEN;
    }
    
    private static final boolean IS_CLOSED = false;
    private static final boolean IS_OPEN = true;
    
    private final AtomicBoolean state = new AtomicBoolean(false);
}
