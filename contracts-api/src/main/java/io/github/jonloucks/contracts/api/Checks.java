package io.github.jonloucks.contracts.api;

/**
 * Checks used internally and supported for external use.
 */
public final class Checks {
    
    /**
     * Utility class instantiation protection
     * Test coverage not possible, java module protections in place
     */
    private Checks() {
        throw new AssertionError("Illegal constructor call.");
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
        return nullCheck(contract, "Contract must be present.");
    }
    
    /**
     * Check if given Contracts is not null or invalid
     * @param contracts the Contracts to check
     * @return a valid Contracts
     */
    public static Contracts contractsCheck(Contracts contracts) {
        return nullCheck(contracts, "Contracts must be present.");
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
        return nullCheck(promisor, "Promisor must be present.");
    }
    
    /**
     * Check if given config is not null or invalid
     *
     * @param config the config to check
     * @param <T>      the type of config
     * @return a valid config
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T configCheck(T config) {
        return nullCheck(config, "Config must be present.");
    }
    
    /**
     * Check if given builder is not null or invalid
     *
     * @param builder the builder to check
     * @param <T>      the type of builder
     * @return a valid builder
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T builderCheck(T builder) {
        return nullCheck(builder, "Builder must be present.");
    }
    
    /**
     * Check if given builder consumer is not null or invalid
     *
     * @param builderConsumer the builder consumer to check
     * @param <T>      the type of builder consumer
     * @return a valid builder consumer
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T builderConsumerCheck(T builderConsumer) {
        return nullCheck(builderConsumer, "Builder consumer must be present.");
    }
    
    /**
     * Check if given type is not null or invalid
     *
     * @param type the type to check
     * @param <T>      the type of type
     * @return a valid type
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T typeCheck(T type) {
        return nullCheck(type, "Type was must be present.");
    }
    
    /**
     * Check if given name is not null or invalid
     *
     * @param name the name to check
     * @param <T>      the type of name
     * @return a valid name
     * @throws IllegalArgumentException when invalid
     */
    public static <T> T nameCheck(T name) {
        return nullCheck(name, "Name was must be present.");
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
        return nullCheck(t, "Message must be present.");
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
            throw new IllegalArgumentException("Message for illegal check must be present.");
        }
        if (failed) {
            throw new IllegalArgumentException(message);
        }
        return t;
    }
    
    /**
     * A simple runtime validation of deployed implementation
     *
     * @param contracts the contracts to check
     */
    public static void validateContracts(Contracts contracts) {
        final Contracts validContracts = contractsCheck(contracts);
        try {
            final Contract<String> contract = Contract.create("validation contract");
            final String deliverableValue = "validate value";
            
            if (validContracts.isBound(contract)) {
                throw new ContractException("Contract should not be bound.");
            }
            final AutoClose bindReturn = validContracts.bind(contract, () -> deliverableValue);
            if (null == bindReturn) {
                throw new ContractException("Contract bind returned null.");
            }
            try (AutoClose closeBinding = bindReturn) {
                final AutoClose ignoredWarning = closeBinding;
                if (!validContracts.isBound(contract)) {
                    throw new ContractException("Contract should have been bound.");
                }
                if (!deliverableValue.equals(validContracts.claim(contract))) {
                    throw new ContractException("Contract claiming not working.");
                }
            }
            if (validContracts.isBound(contract)) {
                throw new ContractException("Contract unbinding not working.");
            }
        } catch (ContractException thrown) {
            throw thrown;
        } catch (RuntimeException thrown) {
            throw new ContractException("Contracts unexpected validation error.", thrown);
        }
    }
}
