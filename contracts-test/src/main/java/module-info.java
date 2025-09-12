/**
 * The Test module for Contracts
 */
module io.github.jonloucks.contracts.test {
    requires org.junit.jupiter.api;
    requires org.mockito;
    requires org.mockito.junit.jupiter;
    requires io.github.jonloucks.contracts.api;
    requires org.junit.jupiter.params;
    
    opens io.github.jonloucks.contracts.test to org.junit.platform.commons;
    
    exports io.github.jonloucks.contracts.test;
}