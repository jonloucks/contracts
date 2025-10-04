package io.github.jonloucks.contracts.api;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.Checks.*;

/**
 * The API agreement for a Contract between the claimee and the claimant.
 * <p>
 * Each Contract instance is a unique key for establishing a binding between
 * an implementation and those that use it.
 * </p>
 * <ul>
 *     <li>Create by custom config  {@link #create(Config)}</li>
 *     <li>Creation by automatic config. {@link #create(String, Object[])}</li>
 *     <li>Used by {@link GlobalContracts#claimContract(Contract)}</li>
 *     <li>Used by {@link GlobalContracts#bindContract(Contract, Promisor)}</li>
 *     <li>Used by {@link Contracts#claim(Contract)}</li>
 *     <li>Used by {@link Contracts#bind(Contract, Promisor)}</li>
 * </ul>
 * <h2>A simple factory</h2>
 * Each call to {@link Contracts#claim(Contract)} will return a new instance.
 * <pre class="code">
 *     <code class="java">
 *  // Save Contract at some agreed upon place
 *  public static final Contract&lt;Contracts&gt; SERVICE = Contract.create("You Choose");
 *  // The promisor needs to be bound before it is claimed.
 *  GlobalContracts.bindContract(SERVICE, () -> new ServiceImpl());
 *  // Ready to be claimed
 *  Contracts service = GlobalContracts.claimContract(SERVICE);
 * </code></pre>
 * If you need a thread specific
 *
 * @param <T> The type of deliverable
 */
public final class Contract<T> {
    
    /**
     * Create a contract with a given name, the rest is automatic.
     * For custom configuration see {@link #create(Config)}
     *
     * @param name          the name for the contract, null is not allowed
     * @param reifiedArray null is not allowed
     * @param <T>           the type of deliverable for this Contract
     * @return the new Contract
     */
    @SafeVarargs // Required to safely determine the checked type of T
    public static <T> Contract<T> create(String name, T... reifiedArray) {
        return create(detectDeliverableType(reifiedArray), b -> b.name(name));
    }
    
    /**
     * Create a contract with a given name, the rest is automatic.
     * For custom configuration see {@link #create(Config)}
     *
     * @param type          the deliverable class for the Contract
     * @param <T>           the type of deliverable for this Contract
     * @return the new Contract
     */
    public static <T> Contract<T> create(Class<T> type) {
        return create(type, b -> {});
    }
    
    /**
     * Create a contract derived from the given configuration
     *
     * @param config the name for the contract, null is not allowed
     * @param <T>    the type of deliverable for this Contract
     * @return the new Contract
     */
    public static <T> Contract<T> create(Config<T> config) {
        return new Contract<>(config);
    }
    
    /**
     * Create a contract from a class type and an optional builder callback
     * @param type the type of deliverable for this Contract
     * @param builderConsumer the builder callback
     * @return the new Contract
     * @param <T> the type of deliverable for this Contract
     */
    public static <T> Contract<T> create(Class<T> type, Consumer<Config.Builder<T>> builderConsumer) {
        final ContractBuilderImpl<T> builder = new ContractBuilderImpl<>(typeCheck(type));
        builderConsumerCheck(builderConsumer).accept(builder);
        return create(builder);
    }
    
    /**
     * Casts the given object to the return type for this Contract
     * This is used to make sure the value is a checked value and does not sneak passed during erasure
     *
     * @param value the value to cast
     * @return the checked value. Note: null is possible. The Promisor is allowed to return null
     * @throws ClassCastException iif the value can't be cast to the return type.
     */
    public T cast(Object value) {
        return config.cast(value);
    }
    
    /**
     * @return the contract name
     */
    public String getName() {
        return config.name();
    }
    
    /**
     * Note: Do not rely on this being a java class name
     * Note: The actual class is never exposed and is by design.
     *
     * @return the type of deliverable for this contract.
     */
    public String getTypeName() {
        return config.typeName();
    }
    
    /**
     * When replaceable a new binding can replace in an existing one
     * The default is false
     *
     * @return true if replaceable
     */
    public boolean isReplaceable() {
        return config.isReplaceable();
    }
    
    @Override
    public String toString() {
        return "Contract[id=" + id + ", name=" + getName() + ", type=" + getTypeName() + "]";
    }
    
    /**
     * The configuration for creating a custom Contract.
     * The required function is {@link Config#cast(Object)} which is plays
     * a key role in ensuring unchecked or unsafe instances do not escape
     *
     * @param <T> The Contract deliverable type
     */
    @FunctionalInterface
    public interface Config<T> {
        
        /**
         * Ensure an instance is of type T (or descendant)
         *
         * @param instance the value to cast to type T
         * @return the value, null is allowed
         * @throws ClassCastException when type of instance is not correct
         */
        T cast(Object instance);
        
        /**
         * User defined name for this contract.
         * Note: Do not rely on this being a java class name
         *
         * @return the type name
         */
        default String name() {
            return "";
        }
        
        /**
         * The type of deliverable for this contract.
         * Note: Do not rely on this being a java class name
         *
         * @return the type name, null is illegal
         */
        default String typeName() {
            return "";
        }
        
        /**
         * When replaceable a new binding can replace in an existing one
         * The default is false
         *
         * @return true if replaceable
         */
        default boolean isReplaceable() {
            return false;
        }
        
        /**
         * Partial builder to streamline custom contracts
         * @param <T> The Contract deliverable type
         */
        interface Builder<T> extends Config<T> {
            /**
             * Set the Contract name
             * @param name the new name
             * @return this builder
             */
            Builder<T> name(String name);
            
            /**
             * Set the Contract typeName
             * @param typeName the new type name
             * @return this builder
             */
            Builder<T> typeName(String typeName);
            
            /**
             * Set if the Contract binding can be replaced
             * @param replaceable true allows a Contract binding to be replaced
             * @return this builder
             */
            Builder<T> replaceable(boolean replaceable);
        }
    }
    
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    
    private final int id = ID_GENERATOR.getAndIncrement();
    private final Config<T> config;
    
    private Contract(Config<T> config) {
        this.config = configCheck(config);
        nameCheck(config.name());
        nullCheck(config.typeName(), "Config type must be present.");
    }
    
    //Since arrays are reified the following is safe and checked
    @SuppressWarnings("unchecked")
    private static <T> Class<T> detectDeliverableType(T ... reifiedArray) {
        final T[] validReifiedArray = nullCheck(reifiedArray, "Reified array must be present.");
        return (Class<T>) validReifiedArray.getClass().getComponentType();
    }
}
