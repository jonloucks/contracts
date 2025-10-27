package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.function.Executable;

import java.io.Serializable;
import java.lang.reflect.*;
import java.time.Duration;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.Checks.*;
import static java.lang.Character.isUpperCase;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contracts testing tools.
 * These utilities are supported for public use.
 * They will follow the symantec versioning just like the production code
 */
@SuppressWarnings("DataFlowIssue")
public final class Tools {
    private Tools() {
        throw new AssertionError("Illegal constructor call");
    }
    
    /**
     * For testing assertions methods
     *
     * @param executable the test that is expected to fail
     */
    public static void assertFails(Executable executable) {
        final Executable validExecutable = nullCheck(executable, "Executable must be present.");
        final AssertionError thrown = assertThrows(AssertionError.class, validExecutable);
        assertObject(thrown);
        assertNotNull(thrown.getMessage());
    }
    
    /**
     * Assert that an object complies with basic expectations
     *
     * @param object the object to check
     */
    public static void assertObject(Object object) {
        final class Unknown {}
        final Unknown unknown = new Unknown();
        
        assertNotNull(object, "object was null.");
        
        //noinspection SimplifiableAssertion,ConstantValue
        assertAll(
            () -> assertEquals(object.hashCode(), object.hashCode(), "Hash codes should not change."),
            () -> assertNotNull(object.toString(), "Object toString() was null."),
            () -> assertFalse(object.equals(null), "Object.equals(null) should be false."),
            () -> assertFalse(object.equals(unknown), "Object.equals(unknown) should be false.")
        );
    }
    
    /**
     * Asserts that a class can NOT be instantiated
     *
     * @param theClass the class to check
     */
    public static void assertInstantiateThrows(Class<?> theClass) {
        final Class<?> validClass = typeCheck(theClass);
        final Throwable thrown = assertThrows(Throwable.class, () -> {
            final Constructor<?> constructor = validClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    
        assertTrue(thrown instanceof IllegalAccessException ||
            thrown instanceof InaccessibleObjectException ||
            thrown instanceof InvocationTargetException ||
            thrown instanceof NoSuchMethodException ||
            thrown instanceof AssertionError
            , "Exception thrown not expected " + thrown.getClass().getName() + ".");
    }
    
    /**
     * Assert the exception matches the exact specifications
     *
     * @param thrown the thrown exception
     * @param cause the cause of the thrown exception
     * @param reason the reason (message) of the exception
     */
    public static void assertThrown(Throwable thrown, Throwable cause, String reason) {
        assertObject(thrown);
        
        assertAll(
            () -> assertMessage(thrown.getMessage()),
            () -> assertEquals(cause, thrown.getCause(), "The cause should match."),
            () -> assertEquals(reason, thrown.getMessage(), "The reason should match."),
            () -> assertNotNull(thrown.toString(), "The string should not be null.")
        );
    }
    
    /**
     * Assert the error message complies with best practices
     *
     * @param message the message to check
     */
    public static void assertMessage(String message) {
        assertNotNull(message, "Message must be present.");
        assertFalse(message.isEmpty(), "Message should not be empty.");
        assertTrue(isUpperCase(message.charAt(0)), "Message should start with a upper case character.");
        assertEquals('.', message.charAt(message.length()-1), "Message should end with a dot: " + message + ".");
    }
    
    /**
     * Assert the thrown exception complies with best practices
     *
     * @param thrown the thrown exception
     */
    public static void assertThrown(Throwable thrown) {
        assertNotNull(thrown, "Thrown must be present.");
        
        assertThrown(thrown, thrown.getCause(), thrown.getMessage());
    }
    
    /**
     * Assert the thrown exception complies with best practices
     * and has matching cause
     *
     * @param thrown the thrown exception
     * @param cause the cause
     */
    public static void assertThrown(Throwable thrown, Throwable cause) {
        assertNotNull(thrown, "Thrown must be present.");
        
        assertThrown(thrown, cause, thrown.getMessage());
    }
    
    /**
     * Assert the thrown exception complies with best practices
     * and a matching reason
     *
     * @param thrown the thrown exception
     * @param reason the expected message or reason
     */
    public static void assertThrown(Throwable thrown, String reason) {
        assertNotNull(thrown, "Thrown must be present.");
        
        assertThrown(thrown, thrown.getCause(), reason);
    }
    
    /**
     *  Run an execution block and validated it throws the expected type of exception
     *
     * @param possibleType the possible type of exception
     * @param executable the executable block which may throw the possible exception type
     * @param <E> the type of exception
     */
    public static <E extends Throwable> void assertThrown(Class<E> possibleType, Executable executable) {
        assertThrown(assertThrows(possibleType, executable));
    }
    
    /**
     * Run an execution block and validated it throws the expected type of exception, and it has
     * a matching reason
     *
     * @param possibleType the possible type of exception
     * @param executable the executable block which may throw the possible exception type
     * @param reason the expected error message
     * @param <E> the type of exception
     */
    public static <E extends Throwable> void assertThrown(Class<E> possibleType, Executable executable, String reason) {
        assertThrown(assertThrows(possibleType, executable), reason);
    }
    
    /**
     * For scenarios that may or may not throw an exception. This can occur in where the behavior
     * is undefined, but if the implementation decides to throw an exception it must be a certain type
     *
     * @param allowedType the allowed possible thrown type
     * @param executable the block of code which may throw an exception
     * @return the optional thrown
     * @param <E> the type of error message
     */
    public static <E extends Throwable> Optional<E> assertMayThrow(Class<E> allowedType, Executable executable) {
        final Class<E> validAllowedTyped = typeCheck(allowedType);
        final Executable validExecutable = nullCheck(executable, "Executable must be present.");
        try {
            validExecutable.execute();
            return Optional.empty();
        } catch (Throwable thrown) {
            assertThrownType(validAllowedTyped, thrown, null);
            return Optional.empty();
        }
    }
    
    /**
     * Assert the Throwable is a instance of the allowed type
     *
     * @param allowedType the allowed type
     * @param thrown the actual thrown
     * @param reason the error message if AssertError is thrown
     * @param <E> the type of exception
     */
    public static <E extends Throwable> void assertThrownType(Class<E> allowedType, Throwable thrown, String reason) {
        final Class<E> validAllowedTyped = typeCheck(allowedType);
        final Throwable validThrown = nullCheck(thrown, "Thrown must be present.");
        
        if (validAllowedTyped.isInstance(validThrown)) {
            return;
        }
        throw assertionFailure() //
            .message(reason) //
            .expected(validAllowedTyped) //
            .actual(validThrown.getClass()) //
            .reason("Unexpected exception type thrown.") //
            .cause(validThrown) //
            .build();
    }
    
    /**
     * Assert a Contract is valid
     *
     * @param contract the contract to check
     * @param config the expected configuration
     * @param valid  a valid value
     * @param <T> the type of deliverable
     */
    public static <T> void assertContract(Contract<T> contract, Contract.Config<T> config, T valid) {
        assertNotNull(contract, "Contract must not be null.");

        assertAll(
            () -> assertObject(contract),
            () -> assertSame(config.cast(valid),contract.cast(valid), "Casting a valid value should work."),
            () -> assertNull(contract.cast(null), "Casting null should return null."),
            () -> assertThrows(ClassCastException.class, () -> contract.cast(System.class), "Invalid cast should thrown."),
            () -> assertSame(config.typeName(), contract.getTypeName(), "Contract type mismatch."),
            () -> assertSame(config.name(), contract.getName(), "Contract name mismatch."),
            () -> assertSame(config.isReplaceable(), contract.isReplaceable(), "Contract replacement mismatch.")
        );
    }
    
    /**
     * When cleaning before and after tests.
     * The strategy is to execute all sanitizers, ignoring any errors.
     *
     * @param sanitizers the things to sanitize
     */
    public static void sanitize(Executable... sanitizers) {
        if(ofNullable(sanitizers).isPresent()) {
            for (Executable sanitizer : sanitizers) {
                if (ofNullable(sanitizer).isPresent()) {
                    try {
                        sanitizer.execute();
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }
    
    /**
     * Clean state from the testing context.
     */
    public static void clean() {
        sanitize(()-> {
            final Contracts.Config config = new Contracts.Config() {};
            final ServiceLoader<? extends ContractsFactory> loader = ServiceLoader.load(config.serviceLoaderClass());
            loader.reload();
        });
    }
    
    /**
     * Do nothing for a period of time (Non-busy-wait)
     *
     * @param duration how long to sleep
     */
    public static void sleep(Duration duration) {
        final Duration validDuration = nullCheck(duration, "Duration must be present.");
        final CountDownLatch latch = new CountDownLatch(1);
        
        illegalCheck(validDuration, validDuration.isNegative(), "Duration must not be negative.");
        if (validDuration.isZero()) {
            return;
        }
        
        try {
            assertFalse(latch.await(validDuration.toMillis(), TimeUnit.MILLISECONDS));
        } catch (InterruptedException ignored) {
        }
    }
    
    /**
     * Create a replaceable Contract for test scenarios
     *
     * @param type the type of contract
     * @return the new Contract
     * @param <T> the deliverable type of the Contract
     */
    public static <T> Contract<T> createReplaceableContract(Class<T> type) {
        return Contract.create(new Contract.Config<>() {
            @Override
            public T cast(Object instance) {
                return type.cast(instance);
            }
            @Override
            public boolean isReplaceable() {
                return true;
            }
        });
    }
    
    /**
     * Assert the given class is implements Serializable and complies with best practices.
     *
     * @param clazz the class the check
     */
    public static void assertIsSerializable(Class<?> clazz) {
        assertTrue(Serializable.class.isAssignableFrom(clazz), "Class must implement Serializable.");
        try {
            final Field serialVersionUIDField = clazz.getDeclaredField("serialVersionUID");
            assertNotNull(serialVersionUIDField, "The serialVersionUID field must exist.");
            assertTrue(Modifier.isStatic(serialVersionUIDField.getModifiers()), "The serialVersionUID must be static.");
            assertTrue(Modifier.isFinal(serialVersionUIDField.getModifiers()), "The serialVersionUID must be final.");
            assertEquals(long.class, serialVersionUIDField.getType(), "The serialVersionUID must be of type long.");
        }  catch (NoSuchFieldException ignored) {
            fail("Unable to find serialVersionUID field in class " + clazz.getName() + ".");
        }
    }
    
    /**
     * Enclosure that receives a new Contracts for testing
     * Note: The Contracts will be automatically closed after consumer block returns
     *
     * @param consumerBlock the consumer of the new Contracts
     */
    public static void withContracts(Consumer<Contracts> consumerBlock) {
        final Contracts.Config config = new Contracts.Config() {
            @Override
            public boolean useShutdownHooks() {
                return false;
            }
        };
        withContracts(config, consumerBlock);
    }
    
    /**
     * Enclosure that receives a new Contracts for testing
     * Note: The Contracts will be automatically closed after consumer block returns
     *
     * @param config The configuration for the new Contracts
     * @param consumerBlock the consumer of the new Contracts
     */
    public static void withContracts(Contracts.Config config, Consumer<Contracts> consumerBlock) {
        final Contracts.Config validConfig = configCheck(config);
        final Consumer<Contracts> validConsumerBlock = nullCheck(consumerBlock, "Block must be present.");
        final Contracts contracts = GlobalContracts.createContracts(validConfig);
        
        try (AutoClose closeContracts = contracts.open()) {
            ignore(closeContracts);
            validConsumerBlock.accept(contracts);
        }
    }
    
    /**
     * Closes the AutoClose, used to avoid getting explicit close warnings in tests.
     *
     * @param autoClose the instance to close
     */
    public static void implicitClose(AutoClose autoClose) {
        autoClose.close();
    }
    
    /**
     * Used to avoid warnings about unused variables in tests. try-with-resource unused, which is NOT a good warning.
     *
     * @param ignored not used
     */
    @SuppressWarnings({"unused", "EmptyMethod"})
    public static void ignore(Object ignored) {
    }
}
