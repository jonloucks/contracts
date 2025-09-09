package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;

public interface Decoy<T> extends Promisor<T>, ServiceFactory, Service, Startup, Shutdown {
}
