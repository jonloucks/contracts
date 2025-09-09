package io.github.jonloucks.contracts.api;

public interface Service extends Startup, Shutdown {
    
    /**
     * Claim the deliverable from a bound contract.
     *
     * @param contract the contract to claim
     * @param <T> type of value returned
     * @return the value returned by the bound Promisor. A Promisor can return null
     * @throws ContractException if Promisor binding does not exist for the contract
     * @throws SecurityException if permission is denied
     */
    <T> T claim(Contract<T> contract);
    
    /**
     * Checks if the contract is bound to a Promisor
     * @param contract the contract to check
     * @return true iif bound
     */
    boolean isBound(Contract<?> contract);
    
    /**
     * Establish a binding between a Contract and a Promisor
     *
     * @param contract the contract to bind the Promisor
     * @param promisor the Promisor for the given contract
     * @return Use to release (unbind) this contract
     * @param <T> The type of the value returned by the promisor
     * @throws ContractException when contract is already bound and can't be replaced
     * @throws SecurityException when permission to bind is denied
     */
    <T> Shutdown bind(Contract<T> contract, Promisor<T> promisor);
    
    interface Config {
        default boolean useShutdownHooks() {
            return true;
        }
        default boolean useReflection() {
            return true;
        }
        
        default boolean useServiceLoader() {
            return true;
        }

        default Class<? extends ServiceFactory> serviceLoaderClass() {
            return ServiceFactory.class;
        }
        
        default String reflectionClassName() {
            return "io.github.jonloucks.contracts.impl.ServiceFactoryImpl";
        }
    }
}
