import io.github.jonloucks.contracts.api.ContractsFactory;
import io.github.jonloucks.contracts.impl.ContractsFactoryImpl;

/**
 * The implementation module for Contracts
 */
module io.github.jonloucks.contracts.impl {
    requires io.github.jonloucks.contracts.api;
    
    opens io.github.jonloucks.contracts.impl to io.github.jonloucks.contracts.api;
    
    provides ContractsFactory with ContractsFactoryImpl;
}