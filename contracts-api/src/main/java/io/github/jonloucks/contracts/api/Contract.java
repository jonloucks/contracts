package io.github.jonloucks.contracts.api;

import java.util.concurrent.atomic.AtomicInteger;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

public final class Contract<T> {
    
    /**
     * Create a contract with a given name and type is automatically detected.
     * <p></p>
     * For custom configuration see {@link #create(Config)}
     * @param name the name for the contract, null is not allowed
     * @param instanceArray null is not allowed
     * @return the new Contract
     * @param <T> the type of deliverable for this Contract
     */
    @SafeVarargs // Required to safely determine the checked type of T
    public static <T> Contract<T> create(String name, T... instanceArray) {
        return safelyGetTypeAndCreate(name, instanceArray);
    }
    
    /**
     * Create a contract derived from the given configuration
     * <p></p>
     * @param config the name for the contract, null is not allowed
     * @return the new Contract
     * @param <T> the type of deliverable for this Contract
     */
    public static <T> Contract<T> create(Config<T> config) {
        return new Contract<>(config);
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
     * @return the type of deliverable for this contract.
     */
    public String getTypeName() {
        return config.typeName();
    }
    
    /**
     * When replaceable a new binding can replace in an existing one
     * The default is false
     * @return true if replaceable
     */
    public boolean isReplaceable() {
        return config.isReplaceable();
    }
    
    @Override
    public String toString() {
        return "Contract[id=" + id + ", name=" + getName() + ", type=" + getTypeName() + "]";
    }
    
    @FunctionalInterface
    public interface Config<T> {
        
        /**
         * Safely cast object to type T
         * @param instance the value to cast to type T
         * @return the value, null is allowed
         * @throws ClassCastException when type of instance is not correct
         */
        T cast(Object instance);
        
        /**
         * User defined name for this contract.
         * Note: Do not rely on this being a java class name
         * @return the type name
         */
        default String name() {
            return "";
        }
        
        /**
         * The type of deliverable for this contract.
         * Note: Do not rely on this being a java class name
         * @return the type name, null is illegal
         */
        default String typeName() {
            return "";
        }
        
        /**
         * When replaceable a new binding can replace in an existing one
         * The default is false
         * @return true if replaceable
         */
        default boolean isReplaceable() {
            return false;
        }
    }
    
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    
    private final int id = ID_GENERATOR.getAndIncrement();
    private final Config<T> config;
    
    private Contract(Config<T> config) {
        this.config = nullCheck(config, "config was null");
        nullCheck(config.name(), "config name was not present");
        nullCheck(config.typeName(), "config type was not present");
    }
    
    private static <T> Contract<T> safelyGetTypeAndCreate(String name, T[] instanceArray) {
        final T[] validInstanceArray = nullCheck(instanceArray, "instance array was not present");
        final String validName = nullCheck(name, "name was not present");
        final Class<T> validDeliverableType = detectDeliverableType(validInstanceArray);
        
        return Contract.create(new Contract.Config<>() {
            @Override
            public String name() {
                return validName;
            }
            @Override
            public String typeName() {
                return validDeliverableType.getTypeName();
            }
            
            @Override
            public T cast(Object instance) {
                return validDeliverableType.cast(instance);
            }
        });
    }
    //Since arrays are reified the following is safe and checked
    @SuppressWarnings("unchecked")
    private static <T> Class<T> detectDeliverableType(T[] validInstanceArray) {
        return nullCheck((Class<T>) validInstanceArray.getClass().getComponentType(), "unable to capture type");
    }
}
