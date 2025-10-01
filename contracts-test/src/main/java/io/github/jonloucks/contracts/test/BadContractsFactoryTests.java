package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.Contracts;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
public interface BadContractsFactoryTests {
    @Test
    default void badContractsFactory_HasProtectedConstructor() throws Throwable {
        final Class<?> klass = Class.forName(BadContractsFactory.class.getCanonicalName());
        final Constructor<?> constructor = klass.getDeclaredConstructor();
        constructor.setAccessible(true);
        final int modifiers = constructor.getModifiers();
        
        assertFalse(Modifier.isPublic(modifiers), "constructor should not be public.");
    }
    
    @Test
    default void badContractsFactory_HasPrivateConstructor() throws Throwable {
        final BadContractsFactory badContractsFactory = new BadContractsFactory();
        final Contracts.Config config = new Contracts.Config(){};
        final Exception thrown = assertThrows(Exception.class, () -> {
            badContractsFactory.create(config);
        });
        
        assertObject(thrown);
    }
}
