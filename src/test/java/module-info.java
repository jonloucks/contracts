import io.github.jonloucks.contracts.api.ContractsFactory;

module io.github.jonloucks.contracts.test.runtests {
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.contracts.impl;
    requires transitive io.github.jonloucks.contracts;
    requires transitive org.junit.jupiter.api;
    requires transitive org.mockito.junit.jupiter;
    requires transitive org.mockito;
    
    uses ContractsFactory;

    exports io.github.jonloucks.contracts.runtests to org.junit.platform.commons;
}