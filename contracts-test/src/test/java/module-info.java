module io.github.jonloucks.contracts.test.run {
    requires io.github.jonloucks.contracts.test;
    requires org.junit.jupiter.api;
    requires org.mockito.junit.jupiter;
    
    exports io.github.jonloucks.contracts.test.run to org.junit.platform.commons;
    
}