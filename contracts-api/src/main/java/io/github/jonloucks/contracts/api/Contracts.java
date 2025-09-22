package io.github.jonloucks.contracts.api;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

/**
 * Provides access to the shared singleton Contracts service
 */
public final class Contracts {
    
    /**
     * Claim the deliverable from a bound contract.
     *
     * @param contract the contract to claim
     * @param <T>      type of value returned
     * @return the value returned by the bound Promisor. A Promisor can return null
     * @throws ContractException if Promisor binding does not exist for the contract
     * @throws SecurityException if permission is denied
     * 
     * @see Service#claim(Contract) 
     */
    public static <T> T claimContract(Contract<T> contract) {
        return CONTRACTS.service.claim(contract);
    }
    
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
    public static <T> AutoClose bindContract(Contract<T> contract, Promisor<T> promisor) {
        return CONTRACTS.service.bind(contract, promisor);
    }
    
    /**
     * Checks if the contract is bound to a Promisor
     *
     * @param contract the contract to check
     * @param <T>      The type of the value returned by the promisor
     * @return true iif bound
     */
    public static <T> boolean isContractBound(Contract<T> contract) {
        return CONTRACTS.service.isBound(contract);
    }
    
    /**
     * Return the global Contracts service
     * @return the service
     */
    public static Service getService() {
        return CONTRACTS.service;
    }
    
    /**
     * Create a standalone Contracts service.
     * Note: Services created from this method are destink any that used internally
     * <p>
     * Caller is responsible for invoking open() before use and close when no longer needed
     * </p>
     *
     * @param serviceConfig the service configuration
     * @return the new service
     */
    public static Service createService(Service.Config serviceConfig) {
        final Service.Config validServiceConfig = nullCheck(serviceConfig, "Service config was null");
        
        final ServiceFactoryFinder factoryFinder = new ServiceFactoryFinder(serviceConfig);
        final ServiceFactory serviceFactory = nullCheck(factoryFinder.createServiceFactory(), "createServiceFactory() was null");
        return nullCheck(serviceFactory.createService(validServiceConfig), "createService() was null");
    }
    
    private static final Contracts CONTRACTS = new Contracts();
    
    private final Service service;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final AutoClose close;
    
    private Contracts() {
        this.service = createService(new Service.Config() {});
        this.close = service.open();
    }
}
