/**
 * Includes all components for the smoke app
 */
module io.github.jonloucks.contracts.smoke {
    requires transitive io.github.jonloucks.contracts;
    
    uses io.github.jonloucks.contracts.api.ContractsFactory;
    
    exports io.github.jonloucks.contracts.smoke;
}