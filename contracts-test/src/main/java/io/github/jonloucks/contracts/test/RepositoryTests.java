package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.api.GlobalContracts.*;
import static io.github.jonloucks.contracts.test.RepositoryTests.RepositoryTestsTool.runWithScenario;
import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("try")
public interface RepositoryTests {
    
    @Test
    default void repository_Factory() {
        final Supplier<Repository> repositoryFactory = claimContract(Repository.FACTORY);
        
        assertObject(repositoryFactory);
    }
    
    @Test
    default void repository_Main_UseCase() {
        final Contract<Repository> contract = Contract.create("test repository");
        try (AutoClose ignored = bindContract(contract, () -> claimContract(Repository.FACTORY).get())) {
            final Repository repository = claimContract(contract);
            assertObject(repository);
        }
    }
    
    @Test
    default void repository_check_WithNoRequirements() {
        runWithScenario(repository -> {
            assertDoesNotThrow(repository::check);
        });
    }
    
    @Test
    default void repository_check_WithOneRequirement_Throws() {
        runWithScenario(repository -> {
            final Contract<Integer> contract = Contract.create("a requirement");
            repository.require(contract);
            final ContractException thrown = assertThrows(ContractException.class, repository::check);
            assertThrown(thrown);
        });
    }
    
    @Test
    default void repository_check_WithFulfilledRequirements() {
        runWithScenario(repository -> {
            final Contract<Integer> contract = Contract.create("a requirement");
            repository.require(contract);
            try (AutoClose closeBinding = bindContract(contract, () -> 42)) {
                nullCheck(closeBinding, "warning: [try] workaround");
                
                assertDoesNotThrow(repository::check);
            }
        });
    }
    
    @Test
    default void repository_store_isBound() {
        runWithScenario(repository -> {
            final Contract<String> textContract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(textContract, () -> "x")) {
                nullCheck(closeBinding, "warning: [try] workaround");
                
                assertTrue(isContractBound(textContract), "Contract should have been bound");
            }
            assertFalse(isContractBound(textContract), "Contract should not be bound");
        });
    }
    
    @Test
    default void repository_store_Works() {
        runWithScenario(repository -> {
            final Contract<String> textContract = Contract.create("test text");
            try (AutoClose closeBinding = repository.store(textContract, () -> "x")) {
                nullCheck(closeBinding, "warning: [try] workaround");
                
                final String text = claimContract(textContract);
                assertEquals("x", text, "contract deliverable should match");
            }
        });
    }
    
    @Test
    default void repository_store_Twice_Throws() {
        runWithScenario(repository -> {
            final Contract<String> textContract = Contract.create("test text");
            
            try (AutoClose closeBinding = repository.store(textContract, () -> "x")) {
                nullCheck(closeBinding, "warning: [try] workaround");
                
                final ContractException thrown = assertThrows(ContractException.class, () -> {
                    //noinspection resource
                    repository.store(textContract, () -> "y");
                });
                assertThrown(thrown);
            }
        });
    }
    
    final class RepositoryTestsTool {
        private RepositoryTestsTool() {
        
        }
        
        interface ScenarioConfig extends Consumer<Repository> {
        
        }
        
        static void runWithScenario(ScenarioConfig block) {
            final Contract<Repository> contract = Contract.create("test repository");
            final Promisors promisors = claimContract(Promisors.CONTRACT);
            final Promisor<Repository> promisor = promisors.createLifeCyclePromisor(()->claimContract(Repository.FACTORY).get());
            try (AutoClose closeBinding = bindContract(contract, promisor)) {
                nullCheck(closeBinding, "warning: [try] workaround");
                final Repository repository = claimContract(contract);
                block.accept(repository);
            }
        }
    }
}
