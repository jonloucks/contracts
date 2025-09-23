package io.github.jonloucks.contracts.api;

/**
 * Checks used internally and supported for external use.
 */
public final class Checks {
    
    /**
     * Utility class instantiation protection
     */
    private Checks() {
        throw new AssertionError("Illegal constructor");
    }
    
    /**
     * Check if given Contract is not null or invalid
     *
     * @param contract the Contract to check
     * @param <T>      the deliverable type
     * @return a valid contract
     * @throws IllegalArgumentException when invalid
     */
    public static <T> Contract<T> contractCheck(Contract<T> contract) {
        return nullCheck(contract, "contract was null");
    }
    
    /**
     * Check if given Promisor is not null or invalid
     *
     * @param promisor the Promisor to check
     * @param <T>      the deliverable type
     * @return a valid promisor
     * @throws IllegalArgumentException when invalid
     */
    public static <T> Promisor<T> promisorCheck(Promisor<T> promisor) {
        return nullCheck(promisor, "promisor was null");
    }
    
    /**
     * Check if given message is not null or invalid
     *
     * @param t   the message to check
     * @param <T> the type of message (normally a String)
     * @return a valid message
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T messageCheck(T t) {
        return nullCheck(t, "Message was null");
    }
    
    /**
     * Check if given instance is not null
     *
     * @param t       the instance to check
     * @param message the message used if an exception is thrown
     * @param <T>     the type of instance
     * @return the value passed
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T nullCheck(T t, String message) {
        return illegalCheck(t, null == t, message);
    }
    
    /**
     * Check if given instance is not null
     *
     * @param t       the instance to check
     * @param failed  if true an IllegalArgumentException is thrown
     * @param message the message used if an exception is thrown
     * @param <T>     the type of instance
     * @return the value passed
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T illegalCheck(T t, boolean failed, String message) {
        if (null == message) {
            throw new IllegalArgumentException("Message for illegal check was null");
        }
        if (failed) {
            throw new IllegalArgumentException(message);
        }
        return t;
    }
    
    public static void validateContracts(Contracts contracts) {
        final Contracts validContracts = nullCheck(contracts, "contracts was null");
        final Contract<String> contract = Contract.create("validation contract");
        final String deliverableValue = "validate value";
        
        if (contracts.isBound(contract)) {
            throw new ContractException("Contract should not be bound");
        }
        try (AutoClose closeBinding = nullCheck(validContracts.bind(contract, () -> deliverableValue), "bind() was null")) {
            final AutoClose ignoredWarning = closeBinding;
            if (!validContracts.isBound(contract)) {
                throw new ContractException("Contract binding not working");
            }
            if (!deliverableValue.equals(validContracts.claim(contract))) {
                throw new ContractException("Contract claiming not working");
            }
        }
        if (validContracts.isBound(contract)) {
            throw new ContractException("Contract unbinding not working");
        }
    }
}
