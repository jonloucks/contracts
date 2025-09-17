package io.github.jonloucks.contracts;

import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.Shutdown;

/**
 * A placeholder class to make sure dependencies are correct for api and implementation.
 */
public final class Stub {
    
    /**
     * Asserts a basic workflow works.
     */
    public Stub() {
        final Contract<String> contract = Contract.create("contracts.all");
        final Shutdown unbindContract = Contracts.bindContract(contract, () -> "hello");
        try {
            assert "hello".equals(Contracts.claimContract(contract));
        } finally {
            unbindContract.shutdown();
        }
        assert !Contracts.isContractBound(Contract.create("Test"));
    }
}
