package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.test.RepositoryTests.RepositoryTestsTool.runWithScenario;
import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
                ignore(ignored);
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
            assertThrown(ContractException.class, repository::check);
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
            final Contract<String> contract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(contract, () -> "x")) {
                final AutoClose ignored = closeBinding;
                assertTrue(contracts.isBound(contract), "Contract should have been bound");
            }
            assertFalse(contracts.isBound(contract), "Contract should not be bound");
        });
    }
    
    @Test
    default void repository_store_Works() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> contract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(contract, () -> "x")) {
                final AutoClose ignored = closeBinding;
                final String text = contracts.claim(contract);
                assertEquals("x", text, "contract deliverable should match");
            }
        });
    }
    
    @Test
    default void repository_store_WhenClosedTwice_DoesNothing() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> contract = Contract.create("test text");
            try (AutoClose closeStore = repository.store(contract, () -> "x")) {
                assertDoesNotThrow(closeStore::close);
                assertDoesNotThrow(closeStore::close);
            }
        });
    }
    
    @Test
    default void repository_open_WhenCalledTwice_DoesNothing() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> contract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(contract, () -> "y")) {
                final AutoClose ignored = closeBinding;
                try (AutoClose closeRepository = repository.open()) {
                    final AutoClose ignored2 = closeRepository;
                }
                assertEquals("y", contracts.claim(contract), "contract deliverable should not change");
            }
        });
    }
    
    @Test
    default void repository_close_WhenCalledTwice_DoesNothing() {
        withContracts(contracts -> {
            final Repository repository = contracts.claim(Repository.FACTORY).get();
            
            try (AutoClose closeRepository = repository.open()) {
                implicitClose(closeRepository);
                assertDoesNotThrow(closeRepository::close);
            }
        });
    }
    
    @Test
    default void repository_close_ReleasesResources() {
        withContracts(contracts -> {
            final Repository repository = contracts.claim(Repository.FACTORY).get();
            final int count = 10;
            final List<Contract<Integer>> contractList = new ArrayList<>();
            final LinkedList<Integer> expectedOrder = new LinkedList<>();
            final List<Integer> actualOrder = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final Integer deliverable = i;
                final Promisor<Integer> promisor = spy();
                final AtomicInteger referenceCount = new AtomicInteger();
                when(promisor.demand()).thenReturn(deliverable);
                when(promisor.incrementUsage()).thenAnswer((Answer<Integer>) invocation -> referenceCount.incrementAndGet());
                when(promisor.decrementUsage()).thenAnswer((Answer<Integer>) invocation -> {
                    final int currentCount = referenceCount.decrementAndGet();
                    if (currentCount == 0) {
                        actualOrder.add(deliverable);
                    }
                    return currentCount;
                });
                final Contract<Integer> contract = Contract.create("Contact " + deliverable);
                contractList.add(contract);
                expectedOrder.push(deliverable);
                repository.keep(contract, promisor);
            }
            try (AutoClose closeRepository = repository.open()) {
                final AutoClose ignoreCloseRepository = closeRepository;
                for (int i = 0; i < count; i++) {
                    contracts.claim(contractList.get(i));
                }
            }
            assertFalse(actualOrder.isEmpty(), "Actual order should not be empty");
            assertEquals(expectedOrder.size(), actualOrder.size(), "closed promisors count");
            assertEquals(expectedOrder, actualOrder, "closed promisors order");
        });
    }
    
    @Test
    default void repository_keep_WhenCalledTwice_Throws() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> contract = Contract.create("test text");
            
            repository.keep(contract, () -> "x");
            
            assertThrown(ContractException.class, () -> repository.keep(contract, () -> "y"));
        });
    }
    
    @Test
    default void repository_keep_Replace_Works() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> contract = Contract.create(String.class, b -> b.replaceable(true));
            
            try (AutoClose closeFirstBinding = contracts.bind(contract, () -> "x") ) {
                final AutoClose ignoredFirstBinding = closeFirstBinding;
                repository.keep(contract, () -> "y");
                final String text = contracts.claim(contract);
                assertEquals("y", text, "contract deliverable replace should match");
            }
        });
    }
    
    @Test
    default void repository_keep_WhenNotReplaceableAndBound_IsIgnored() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> contract = Contract.create(String.class, b -> b.replaceable(false));
            
            try (AutoClose closeFirstBinding = contracts.bind(contract, () -> "x") ) {
                final AutoClose ignoredFirstBinding = closeFirstBinding;
                repository.keep(contract, () -> "y");
                final String text = contracts.claim(contract);
                assertEquals("x", text, "contract deliverable should not change");
            }
        });
    }
    
    @Test
    default void repository_keep_WhenNotReplaceableAndBoundAnd_BIND_ALWAYS_Throws() {
        runWithScenario(( contracts,repository) -> {
            final Contract<String> contract = Contract.create(String.class, b -> b.replaceable(false));
            
            try (AutoClose closeFirstBinding = contracts.bind(contract, () -> "x") ) {
                final AutoClose ignoredFirstBinding = closeFirstBinding;
                assertThrown(ContractException.class, () -> repository.keep(contract, () -> "y", BindStrategy.ALWAYS));
            }
        });
    }
    
    @Test
    default void repository_InternalCoverage() {
        assertInstantiateThrows(RepositoryTestsTool.class);
    }
    
    final class RepositoryTestsTool {
        private RepositoryTestsTool() {
            throw new AssertionError("Illegal constructor.");
        }
        interface ScenarioConfig extends BiConsumer<Contracts, Repository> {
        
        }
        
        static void runWithScenario(ScenarioConfig block) {
            withContracts(contracts -> {
                final Repository repository = contracts.claim(Repository.FACTORY).get();
                try (AutoClose closeRepository = repository.open()) {
                    ignore(closeRepository);
                    block.accept(contracts, repository);
                }
            });
        }
    }
}
