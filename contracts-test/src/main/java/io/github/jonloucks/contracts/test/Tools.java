package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.function.Executable;

import java.io.Serializable;
import java.lang.reflect.*;
import java.time.Duration;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.Checks.*;
import static java.lang.Character.isUpperCase;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contracts testing tools
 */
@SuppressWarnings("DataFlowIssue")
public final class Tools {
    private Tools() {
        throw new AssertionError("Illegal constructor call");
    }
    
    /**
     * For testing testing
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
            , "Exception thrown not expected " + thrown.getClass().getName());
    }
    
    public static void assertThrown(Throwable thrown, Throwable cause, String reason) {
        assertObject(thrown);
        
        assertAll(
            () -> assertMessage(thrown.getMessage()),
            () -> assertEquals(cause, thrown.getCause(), "The cause should match."),
            () -> assertEquals(reason, thrown.getMessage(), "The reason should match."),
            () -> assertNotNull(thrown.toString(), "The string should not be null.")
        );
    }
    
    public static void assertMessage(String message) {
        assertNotNull(message, "Message must be present.");
        assertFalse(message.isEmpty(), "Message should not be empty.");
        assertTrue(isUpperCase(message.charAt(0)), "Message should start with a upper case character.");
        assertEquals('.', message.charAt(message.length()-1), "Message should end with a dot: " + message + ".");
    }
    
    public static void assertThrown(Throwable thrown) {
        assertNotNull(thrown, "Thrown must be present.");
        
        assertThrown(thrown, thrown.getCause(), thrown.getMessage());
    }
    
    public static void assertThrown(Throwable thrown, Throwable cause) {
        assertNotNull(thrown, "Thrown must be present.");
        
        assertThrown(thrown, cause, thrown.getMessage());
    }
    
    public static void assertThrown(Throwable thrown, String reason) {
        assertNotNull(thrown, "Thrown must be present.");
        
        assertThrown(thrown, thrown.getCause(), reason);
    }
  
    /**
     * Assert a Contract is valid
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
    
    public static void withContracts(Consumer<Contracts> block) {
        final Contracts.Config config = new Contracts.Config() {
            @Override
            public boolean useShutdownHooks() {
                return false;
            }
        };
        withContracts(config, block);
    }
    
    public static void withContracts(Contracts.Config config, Consumer<Contracts> block) {
        final Contracts.Config validConfig = configCheck(config);
        final Consumer<Contracts> validBlock = nullCheck(block, "Block must be present.");
        final Contracts contracts = GlobalContracts.createContracts(validConfig);
        
        try (AutoClose closeContracts = contracts.open()) {
            final AutoClose ignored = closeContracts;
            validBlock.accept(contracts);
        }
    }
    
    public static void implicitClose(AutoClose close) {
        close.close();
    }
}
