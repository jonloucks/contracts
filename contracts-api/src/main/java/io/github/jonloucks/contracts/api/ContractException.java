package io.github.jonloucks.contracts.api;

import static io.github.jonloucks.contracts.api.Checks.*;

public class ContractException extends RuntimeException {
   
    public ContractException(String message) {
        this(message, null);
    }

    public ContractException(String message, Throwable thrown) {
        super(messageCheck(message), thrown);
    }
}
