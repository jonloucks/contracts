package io.github.jonloucks.contracts.all;

import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Contracts;

public final class Module {
    public Module() {
        assert !Contracts.isContractBound(Contract.create("Test"));
    }
}
