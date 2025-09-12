package io.github.jonloucks.contracts.api;

/**
 * The actual implementation used for Contracts itself.
 * It is used to bootstrap Contracts without it knowing the implementation if Service.
 * It does know how to load the default from contracts-impl.
 * However, the design is open to have it replaced with an alternative implementation.
 */
public interface Service extends Startup, Shutdown {
    
    /**
     * Claim the deliverable from a bound contract.
     *
     * @param contract the contract to claim
     * @param <T>      type of value returned
     * @return the value returned by the bound Promisor. A Promisor can return null
     * @throws ContractException if Promisor binding does not exist for the contract
     * @throws SecurityException if permission is denied
     */
    <T> T claim(Contract<T> contract);
    
    /**
     * Checks if the contract is bound to a Promisor
     *
     * @param contract the contract to check
     * @param <T>      The type of the value returned by the promisor
     * @return true iif bound
     */
    <T> boolean isBound(Contract<T> contract);
    
    /**
     * Establish a binding between a Contract and a Promisor
     *
     * @param contract the contract to bind the Promisor
     * @param promisor the Promisor for the given contract
     * @param <T>      The type of the value returned by the promisor
     * @return Use to release (unbind) this contract
     * @throws ContractException when contract is already bound and can't be replaced
     * @throws SecurityException when permission to bind is denied
     */
    <T> Shutdown bind(Contract<T> contract, Promisor<T> promisor);
    
    /**
     * The service configuration
     */
    interface Config {
        
        /**
         * @return if true, shutdown hooks will be added to ensure cleanup of Contracts
         */
        default boolean useShutdownHooks() {
            return true;
        }
        
        /**
         * @return if true, reflection might be used to locate the ServiceFactory
         */
        default boolean useReflection() {
            return true;
        }
        
        /**
         * @return if true, the ServiceLoader might be used to locate the ServiceFactory
         */
        default boolean useServiceLoader() {
            return true;
        }
        
        /**
         * @return the class name to load from the ServiceLoader to find the ServiceFactory
         */
        default Class<? extends ServiceFactory> serviceLoaderClass() {
            return ServiceFactory.class;
        }
        
        /**
         * @return the class name to use if reflection is used to find the ServiceFactory
         */
        default String reflectionClassName() {
            return "io.github.jonloucks.contracts.impl.ServiceFactoryImpl";
        }
    }
}
