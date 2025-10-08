
module io.github.jonloucks.contracts.test.runtests {
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.contracts.impl;
    requires transitive io.github.jonloucks.contracts;

    uses io.github.jonloucks.contracts.api.ContractsFactory;

    exports io.github.jonloucks.contracts.runtests to org.junit.platform.commons;
}