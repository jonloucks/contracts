/**
 * The Test module for Contracts
 */
module io.github.jonloucks.contracts.test {
    requires transitive org.junit.jupiter.api;
    requires transitive org.mockito;
    requires transitive org.mockito.junit.jupiter;
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive org.junit.jupiter.params;
    requires transitive org.junit.platform.commons;
    
    opens io.github.jonloucks.contracts.test to org.junit.platform.commons;
    
    exports io.github.jonloucks.contracts.test;
}