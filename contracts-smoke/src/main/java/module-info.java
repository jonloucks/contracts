/**
 * Includes all default components of the Concurrency library needed for a working deployment.
 */
module io.github.jonloucks.contracts.smoke {
    requires transitive io.github.jonloucks.contracts;
    
    uses io.github.jonloucks.contracts.api.ContractsFactory;
    
    exports io.github.jonloucks.contracts.smoke;
}