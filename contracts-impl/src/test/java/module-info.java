import io.github.jonloucks.contracts.api.ContractsFactory;

module io.github.jonloucks.contracts.impl.test {
    uses ContractsFactory;
    requires org.junit.jupiter.api;
    requires org.mockito.junit.jupiter;
    requires io.github.jonloucks.contracts.api;
    requires io.github.jonloucks.contracts.test;
    
    exports io.github.jonloucks.contracts.impl.test to org.junit.platform.commons;
}