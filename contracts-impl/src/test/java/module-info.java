/**
 * contracts.impl tests
 */
module io.github.jonloucks.contracts.impl.test {
    uses io.github.jonloucks.contracts.api.ContractsFactory;
    
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.contracts.test;
    
    exports io.github.jonloucks.contracts.impl.test to org.junit.platform.commons;
}