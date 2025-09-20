package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Service;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
public interface BadServiceFactoryTests {
    @Test
    default void badServiceFactory_HasProtectedConstructor() throws Throwable {
        final Class<?> klass = Class.forName(BadServiceFactory.class.getCanonicalName());
        final Constructor<?> constructor = klass.getDeclaredConstructor();
        constructor.setAccessible(true);
        final int modifiers = constructor.getModifiers();
        
        assertFalse(Modifier.isPublic(modifiers), "constructor should not be public.");
    }
    
    @Test
    default void badServiceFactory_HasPrivateConstructor() throws Throwable {
        final BadServiceFactory badServiceFactory = new BadServiceFactory();
        final Service.Config config = new Service.Config(){};
        final Exception thrown = assertThrows(Exception.class, () -> {
            //noinspection resource
            badServiceFactory.createService(config);
        });
        
        assertObject(thrown);
    }
}
