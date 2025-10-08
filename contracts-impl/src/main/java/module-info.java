/**
 * The implementation module for Contracts
 */
module io.github.jonloucks.contracts.impl {
    requires io.github.jonloucks.contracts.api;
    
    opens io.github.jonloucks.contracts.impl to io.github.jonloucks.contracts.api;
    
    provides io.github.jonloucks.contracts.api.ContractsFactory with io.github.jonloucks.contracts.impl.ContractsFactoryImpl;
}