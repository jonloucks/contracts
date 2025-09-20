module io.github.jonloucks.contracts.test.runtests {
    requires io.github.jonloucks.contracts.test;
    requires org.junit.jupiter.api;
    requires org.mockito.junit.jupiter;
    requires io.github.jonloucks.contracts.api;
    requires io.github.jonloucks.contracts.impl;
    requires io.github.jonloucks.contracts;
    requires org.mockito;
    
    uses io.github.jonloucks.contracts.api.ServiceFactory;

    exports io.github.jonloucks.contracts.runtests to org.junit.platform.commons;
}