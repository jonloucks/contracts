package io.github.jonloucks.contracts;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.ContractException;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.api.GlobalContracts.*;
import static java.util.Optional.ofNullable;

/**
 * A placeholder class to make sure dependencies are correct for api and implementation.
 */
public final class Stub {
 
    private Stub() {
        throw new AssertionError("Illegal constructor");
    }
    
    /**
     * Validates basic functionality.
     */
    public static void validate() {
        final Contract<String> contract = Contract.create("validation contract");
        final String deliverableValue = "validate value";
        if (!deliverableValue.equals(contract.cast(deliverableValue))) {
            throw new ContractException("Contract cast not working");
        }
        if (ofNullable(contract.cast(null)).isPresent()) {
            throw new ContractException("Contract cast null not working");
        }
        try (AutoClose closeBinding = bindContract(contract, () -> deliverableValue)){
            nullCheck(closeBinding, "warning: [try] workaround");
            if (!isContractBound(contract)) {
                throw new ContractException("Contract binding not working");
            }
            if (!deliverableValue.equals(claimContract(contract))) {
                throw new ContractException("Contract claiming not working");
            }
        }
        if (isContractBound(contract)) {
            throw new ContractException("Contract unbinding not working");
        }
    }
}
