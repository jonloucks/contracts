package io.github.jonloucks.contracts.api;

import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.contracts.api.PunchBoard.getPunch;

/**
 * Provides access to the shared singleton of a Contracts implementation
 */
public final class GlobalContracts {
    
    /**
     * Claim the deliverable from a bound contract.
     *
     * @param contract the contract to claim
     * @param <T>      type of value returned
     * @return the value returned by the bound Promisor. A Promisor can return null
     * @throws ContractException if Promisor binding does not exist for the contract
     * @throws SecurityException if permission is denied
     * 
     * @see Contracts#claim(Contract)
     */
    public static <T> T claimContract(Contract<T> contract) {
        return INSTANCE.contracts.claim(contract);
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
        return INSTANCE.contracts.bind(contract, promisor);
    }
    
    /**
     * Checks if the contract is bound to a Promisor
     *
     * @param contract the contract to check
     * @param <T>      The type of the value returned by the promisor
     * @return true iif bound
     */
    public static <T> boolean isContractBound(Contract<T> contract) {
        return INSTANCE.contracts.isBound(contract);
    }
    
    /**
     * Creates a Promisor that only calls the source Promisor once and then always
     * returns that value.
     * Note: increment and decrementUsage are relayed to the source promisor.
     *
     * @param promisor the source Promisor
     * @return The new Promisor
     * @param <T> the type of deliverable
     */
    public static <T> Promisor<T> singleton(Promisor<T> promisor) {
        return INSTANCE.promisors.createSingletonPromisor(promisor);
    }
    
    /**
     * Reference counted, lazy loaded, with opt-in 'open' and 'close' invoked on deliverable.
     * Note: increment and decrementUsage are relayed to the source promisor.
     *
     * @param promisor the source promisor
     * @return the new Promisor
     * @param <T> the type of deliverable
     */
    public static <T> Promisor<T> lifeCycle(Promisor<T> promisor) {
        return INSTANCE.promisors.createLifeCyclePromisor(promisor);
    }
    
    /**
     * Return the global instance of Contracts
     * @return the instance
     */
    public static Contracts getInstance() {
        return INSTANCE.contracts;
    }
    
    /**
     * Create a standalone Contracts service.
     * Note: Contracts created from this method are destink any that used internally
     * <p>
     * Caller is responsible for invoking open() before use and close when no longer needed
     * </p>
     *
     * @param config the service configuration
     * @return the new service
     */
    public static Contracts createContracts(Contracts.Config config) {
        final Contracts.Config validConfig = configCheck(config);
        final ContractsFactoryFinder factoryFinder = new ContractsFactoryFinder(config);
        final ContractsFactory contractsFactory = nullCheck(factoryFinder.find(), "Contracts factory not found.");
        return nullCheck(contractsFactory.create(validConfig), "Contracts could not be created.");
    }
    
    private static final GlobalContracts INSTANCE = new GlobalContracts();
    private final Contracts contracts;
    private final Promisors promisors;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final AutoClose close;
    
    private GlobalContracts() {
        this.contracts = createContracts(getPunch(PunchBoard.GLOBAL_CONFIG)
            .orElseThrow(() -> new ContractException("Global Contracts configuration must be present.")));
        this.close = contracts.open();
        validateContracts(contracts);
        this.promisors = contracts.claim(Promisors.CONTRACT);
    }
}
