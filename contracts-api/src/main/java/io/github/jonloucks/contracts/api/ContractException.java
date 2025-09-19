package io.github.jonloucks.contracts.api;

import static io.github.jonloucks.contracts.api.Checks.*;

/**
 * Runtime exception thrown for Contract related problems.
 * For example, when claimed contract is not bound to a promisor.
 */
public class ContractException extends RuntimeException {
    
    private static final long serialVersionUID = 7311228400588901174L;
    
    /**
     * Passthrough for {@link java.lang.RuntimeException#RuntimeException(String)}
     *
     * @param message the message for this exception
     */
    public ContractException(String message) {
        this(message, null);
    }
    
    /**
     * Passthrough for {@link java.lang.RuntimeException#RuntimeException(String, Throwable)}
     *
     * @param message the message for this exception
     * @param thrown  the cause of this exception, null is allowed
     */
    public ContractException(String message, Throwable thrown) {
        super(messageCheck(message), thrown);
    }
}
