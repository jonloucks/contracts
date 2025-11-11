/**
 * Includes all components for testing the smoke app
 */
module io.github.jonloucks.contracts.smoke.test {
    requires transitive io.github.jonloucks.contracts;
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.contracts.smoke;
    
    opens io.github.jonloucks.contracts.smoke.test to org.junit.platform.commons;
    
    exports io.github.jonloucks.contracts.smoke.test;
}