module io.github.jonloucks.contracts.all.test {
    uses io.github.jonloucks.contracts.api.ServiceFactory;
    requires org.junit.jupiter.api;
    requires org.mockito.junit.jupiter;
    requires io.github.jonloucks.contracts.api;
    requires io.github.jonloucks.contracts.test;
    requires io.github.jonloucks.contracts.impl;
    requires io.github.jonloucks.contracts.all;
    
    exports io.github.jonloucks.contracts.all.test to org.junit.platform.commons;
}