/**
 * Module to run tests on test tools
 */
module io.github.jonloucks.contracts.test.run {
    requires transitive io.github.jonloucks.contracts.test;

    exports io.github.jonloucks.contracts.test.run to org.junit.platform.commons;
}