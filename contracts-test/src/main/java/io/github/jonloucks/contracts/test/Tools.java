package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Service;
import io.github.jonloucks.contracts.api.ServiceFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.github.jonloucks.contracts.api.Checks.illegalCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static org.junit.jupiter.api.Assertions.*;

public final class Tools {
    private Tools() {
        throw new AssertionError("Illegal constructor");
    }
    
    public static void assertObject(Object object) {
        class Unknown {}
        Unknown unknown = new Unknown();
        assertNotNull(object, "object was null");
        
        assertAll(
            () -> assertEquals(object.hashCode(), object.hashCode(), "hash codes should not change."),
            () -> assertNotNull(object.toString(), "object toString() was null."),
            () -> assertNotEquals(null, object, "Object.equals(null) should be false."),
            () -> assertNotEquals( unknown, object, "Object.equals(null) should be true.")
        );
    }
    
    /**
     * Checks that given class can not be instantiated
     * @param aClass the class to attempt to instantiate
     */
    public static void assertInstantiateThrows(Class<?> aClass) {
        final Class<?> validClass = nullCheck(aClass, "class was null");;
        final Throwable thrown = assertThrows(Throwable.class, () -> {
            Constructor<?> constructor = validClass.getDeclaredConstructor();
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
    
    public static void assertThrown(Throwable thrown) {
        assertThrown(thrown, (Throwable)null);
    }
    
    public static void assertThrown(Throwable thrown, Throwable cause) {
       assertObject(thrown);
        assertAll(
            () -> assertNotNull(thrown, "The thrown exception should not be null."),
            () -> assertEquals(cause, thrown.getCause(), "The cause should match."),
            () -> assertNotNull(thrown.getMessage(), "The message should not be null."),
            () -> assertNotNull(thrown.toString(), "The string should not be null.")
        );
    }
    
    public static void assertThrown(Throwable thrown, Throwable cause, String message) {
        assertAll(
            () -> assertObject(thrown),
            () -> assertNotNull(thrown, "The thrown exception should not be null."),
            () -> assertEquals(cause, thrown.getCause(), "The cause should match."),
            () -> assertEquals(message, thrown.getMessage(), "The message should match."),
            () -> assertNotNull(thrown.toString(), "The string should not be null.")
        );
    }
    
    public static void assertThrown(Throwable thrown, String message) {
        assertAll(
            () -> assertNotNull(thrown, "The thrown exception should not be null."),
            () -> assertEquals(message, thrown.getMessage(), "The message should match."),
            () -> assertNotNull(thrown.toString(), "The string should not be null.")
        );
    }
    
    public static <T> void assertContract(Contract<T> contract, T valid ) {
        assertAll(
            () -> assertNotNull(contract, "Contract must not be null."),
            () -> assertNotNull(contract.getName(), "Contract name must not be null."),
            () -> assertNotNull(contract.getTypeName(), "Contract type must not be null."),
            () -> assertNull(contract.cast(null), "Cast null should return null."),
            () -> assertSame(valid, contract.cast(valid), "cast valid value."),
            () -> assertThrows(ClassCastException.class, () -> contract.cast(System.class), "Invalid cast should thrown."),
            () -> assertNotNull(contract.toString(), "Contract string must not be null.")
        );
    }
    
    public static <T> void assertContract(Contract<T> contract, Contract.Config<T> config, T valid) {
        assertAll(
            () -> assertContract(contract, valid),
            () -> assertSame(config.cast(valid),contract.cast(valid), "cast valid value."),
            () -> assertSame(config.typeName(), contract.getTypeName(), "Contract type mismatch."),
            () -> assertSame(config.name(), contract.getName(), "Contract name mismatch."),
            () -> assertSame(config.isReplaceable(), contract.isReplaceable(), "Contract replacement mismatch.")
        );
    }
    
    public static void clean() {
        try {
            final Service.Config config = new Service.Config() {};
            final ServiceLoader<? extends ServiceFactory> loader = ServiceLoader.load(config.serviceLoaderClass());
            loader.reload();
        } catch (Throwable ignored) {
        }
    }
    
    /**
     * Sleep for testing, without using busy wait
     * @param duration how long to wait
     */
    public static void sleep(Duration duration) {
        final Duration validDuration = nullCheck(duration, "Duration must not be null");
        final CountDownLatch latch = new CountDownLatch(1);
        
        illegalCheck(validDuration, validDuration.isNegative(), "Duration must not be negative");
        if (validDuration.isZero()) {
            return;
        }
        
        try {
            assertFalse(latch.await(validDuration.toMillis(), TimeUnit.MILLISECONDS));
        } catch (InterruptedException ignored) {
        }
    }
}
