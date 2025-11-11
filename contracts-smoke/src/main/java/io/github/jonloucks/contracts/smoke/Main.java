package io.github.jonloucks.contracts.smoke;

import io.github.jonloucks.contracts.Stub;
import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.GlobalContracts;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static io.github.jonloucks.contracts.api.Checks.configCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;

/**
 * Smoke test, to validate running on real environment
 */
public final class Main {
    
    /**
     * For testing the smoke application
     * @param consumer for the exit code
     * @return the previous system exit method
     */
    public static IntConsumer setSystemExit(IntConsumer consumer) {
        final IntConsumer save = SYSTEM_EXIT;
        SYSTEM_EXIT = consumer;
        return save;
    }
    
    /**
     * The main entry point
     * @param args the program arguments
     */
    public static void main(String[] args) {
        try {
            Stub.validate();
            validateReflectionOnly();
            validateServiceLoaderOnly();
            SYSTEM_EXIT.accept(0);
        } catch (Exception thrown) {
            System.err.println(thrown.getMessage());
            SYSTEM_EXIT.accept(1);
        }
    }
    
    private static void validateReflectionOnly() {
        validateHelper(true, false);
    }
    
    private static void validateServiceLoaderOnly() {
        validateHelper(false, true);
    }
    
    private static void validateHelper(boolean useReflection, boolean useServiceLoader) {
        final Contracts.Config contractsConfig = new Contracts.Config() {
            @Override
            public boolean useReflection() {
                return useReflection;
            }
            @Override
            public boolean useServiceLoader() {
                return useServiceLoader;
            }
        };
        withContracts(contractsConfig, Stub::validate);
    }
  
    @SuppressWarnings({"try"})
    private static void withContracts(Contracts.Config config, Consumer<Contracts> consumerBlock) {
        final Contracts.Config validConfig = configCheck(config);
        final Consumer<Contracts> validConsumerBlock = nullCheck(consumerBlock, "Block must be present.");
        final Contracts contracts = GlobalContracts.createContracts(validConfig);
        
        try (AutoClose ignored = contracts.open()) {
            validConsumerBlock.accept(contracts);
        }
    }

    private Main() {
    
    }
    
    private static IntConsumer SYSTEM_EXIT = System::exit;
}
