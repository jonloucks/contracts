package io.github.jonloucks.contracts;

import io.github.jonloucks.contracts.api.Checks;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.GlobalContracts;

/**
 * A placeholder class to make sure dependencies are correct for api and implementation.
 */
public final class Stub {
    
    /**
     * Utility class instantiation protection
     */
    private Stub() {
        // conflicting standards.  100% code coverage vs throwing exception on instantiation of utility class.
        // Java modules protects agents invoking private methods.
        // There are unit tests that will fail if this constructor is not private
    }
    
    /**
     * Quick validation the Global shared Contracts.
     */
    public static void validate() {
        validate(GlobalContracts.getInstance());
    }
    
    /**
     * Quick validation of basic functionality.
     * @param contracts the contracts to validate
     */
    public static void validate(Contracts contracts) {
        Checks.validateContracts(contracts);
    }
}
