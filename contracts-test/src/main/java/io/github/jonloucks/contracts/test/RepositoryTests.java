package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.test.RepositoryTests.RepositoryTestsTool.runWithScenario;
import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"try", "CodeBlock2Expr"})
public interface RepositoryTests {
    
    @Test
    default void repository_Factory() {
        withContracts(contracts -> {
            final Supplier<Repository> repositoryFactory = contracts.claim(Repository.FACTORY);
            
            assertObject(repositoryFactory);
        });
    }
    
    @Test
    default void repository_Main_UseCase() {
        withContracts(contracts -> {
            final Contract<Repository> contract = Contract.create("test repository");
            try (AutoClose ignored = contracts.bind(contract, () -> contracts.claim(Repository.FACTORY).get())) {
                final Repository repository = contracts.claim(contract);
                assertObject(repository);
            }
        });
    }
    
    @Test
    default void repository_check_WithNoRequirements() {
        runWithScenario( ( contracts,repository) -> assertDoesNotThrow(repository::check));
    }
    
    @Test
    default void repository_check_WithOneRequirement_Throws() {
        runWithScenario(( contracts,repository) -> {
            final Contract<Integer> contract = Contract.create("a requirement");
            repository.require(contract);
            final ContractException thrown = assertThrows(ContractException.class, repository::check);
            assertThrown(thrown);
        });
    }
    
    @Test
    default void repository_check_WithFulfilledRequirements() {
        runWithScenario(( contracts,repository) -> {
            final Contract<Integer> contract = Contract.create("a requirement");
            repository.require(contract);
            try (AutoClose closeBinding = contracts.bind(contract, () -> 42)) {
                final AutoClose ignored = closeBinding;
                assertDoesNotThrow(repository::check);
            }
        });
    }
    
    @Test
    default void repository_store_isBound() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> textContract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(textContract, () -> "x")) {
                final AutoClose ignored = closeBinding;
                assertTrue(contracts.isBound(textContract), "Contract should have been bound");
            }
            assertFalse(contracts.isBound(textContract), "Contract should not be bound");
        });
    }
    
    @Test
    default void repository_store_Works() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> textContract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(textContract, () -> "x")) {
                final AutoClose ignored = closeBinding;
                final String text = contracts.claim(textContract);
                assertEquals("x", text, "contract deliverable should match");
            }
        });
    }
    
    @Test
    default void repository_store_WhenClosedTwice_DoesNothing() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> textContract = Contract.create("test text");
            try (AutoClose closeStore = repository.store(textContract, () -> "x")) {
                assertDoesNotThrow(closeStore::close);
                assertDoesNotThrow(closeStore::close);
            }
        });
    }
    
    @Test
    default void repository_open_WhenCalledTwice_DoesNothing() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> textContract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(textContract, () -> "y")) {
                final AutoClose ignored = closeBinding;
                try (AutoClose closeRepository = repository.open()) {
                    final AutoClose ignored2 = closeRepository;
                }
                assertEquals("y", contracts.claim(textContract), "contract deliverable should not change");
            }
        });
    }
    
    @Test
    default void repository_close_WhenCalledTwice_DoesNothing() {
        withContracts(contracts -> {
            final Repository repository = contracts.claim(Repository.FACTORY).get();
            
            try (AutoClose closeRepository = repository.open()) {
                closeRepository.close();
                assertDoesNotThrow(closeRepository::close);
            }
        });
    }
    
    @Test
    default void repository_keep_WhenCalledTwice_Throws() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> textContract = Contract.create("test text");
            
            repository.keep(textContract, () -> "x");
            
            final ContractException thrown = assertThrows(ContractException.class, () -> {
                repository.keep(textContract, () -> "y");
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void repository_keep_Replace_Works() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> textContract = Contract.create(String.class, b -> b.replaceable(true));
            
            try (AutoClose closeFirstBinding = contracts.bind(textContract, () -> "x") ) {
                final AutoClose ignoredFirstBinding = closeFirstBinding;
                repository.keep(textContract, () -> "y");
                final String text = contracts.claim(textContract);
                assertEquals("y", text, "contract deliverable replace should match");
            }
        });
    }
    
    @Test
    default void repository_keep_WhenNotReplaceableAndBound_IsIgnored() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> textContract = Contract.create(String.class, b -> b.replaceable(false));
            
            try (AutoClose closeFirstBinding = contracts.bind(textContract, () -> "x") ) {
                final AutoClose ignoredFirstBinding = closeFirstBinding;
                repository.keep(textContract, () -> "y");
                final String text = contracts.claim(textContract);
                assertEquals("x", text, "contract deliverable should not change");
            }
        });
    }
    
    @Test
    default void repository_InternalCoverage() {
        assertInstantiateThrows(RepositoryTestsTool.class);
    }
    
    final class RepositoryTestsTool {
        private RepositoryTestsTool() {
            throw new AssertionError("Illegal constructor");
        }
        interface ScenarioConfig extends BiConsumer<Contracts, Repository> {
        
        }
        
        static void runWithScenario(ScenarioConfig block) {
            withContracts(contracts -> {
                final Repository repository = contracts.claim(Repository.FACTORY).get();
                try (AutoClose closeRepository = repository.open()) {
                    final AutoClose ignored = closeRepository;
                    block.accept(contracts, repository);
                }
            });
        }
    }
}
