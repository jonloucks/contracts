package io.github.jonloucks.contracts.impl;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.concurrent.atomic.AtomicReference;

final class CloserImpl {
    CloserImpl() {
    
    }
    
    void close() {
        final AutoClose close = reference.get();
        if (close != null && reference.compareAndSet(close, null)) {
            close.close();
        }
    }
    
    void set(AutoClose autoClose) {
        if (autoClose == reference.get()) {
            return;
        }
        try {
            close();
        } finally {
            reference.set(autoClose);
        }
    }
    
    private final AtomicReference<AutoClose> reference = new AtomicReference<>();
}
