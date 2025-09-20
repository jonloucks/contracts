package io.github.jonloucks.contracts;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Contracts;

/**
 * A placeholder class to make sure dependencies are correct for api and implementation.
 */
public final class Stub {
    
    /**
     * Asserts a basic workflow works.
     */
    public Stub() {
        final Contract<String> contract = Contract.create("contracts.all");
        try (AutoClose ignored = Contracts.bindContract(contract, () -> "hello")){
            assert "hello".equals(Contracts.claimContract(contract));
        }
        assert !Contracts.isContractBound(Contract.create("Test"));
    }
}
