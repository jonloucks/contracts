/**
 * Includes all default components of the Contracts library needed for a working deployment.
 */
module io.github.jonloucks.contracts {
    requires io.github.jonloucks.contracts.api;
    requires io.github.jonloucks.contracts.impl;
    
    exports io.github.jonloucks.contracts;
}