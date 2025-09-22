package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.api.Contracts.bindContract;
import static io.github.jonloucks.contracts.test.LifeCyclePromisorTests.ConcurrencyTestsTool.*;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.contracts.test.Tools.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface LifeCyclePromisorTests {
    

    @Test
    default void promisors_createLifeCyclePromisor_WithNull_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            promisors.createLifeCyclePromisor(null)
        );
        
        assertThrown(thrown);
    }
    
    @Test
    default void promisors_LifeCyclePromisor_get_WithUsage_Throws() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createLifeCyclePromisor(() -> "abc");
        
        final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            promisor.demand();
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void promisors_createLifeCyclePromisor_WithNullDeliverable_Works() {
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        final Promisor<String> promisor = promisors.createLifeCyclePromisor(()->null);
        promisor.incrementUsage();
        
        assertNotNull(promisor, "should not return null.");
        assertNull(promisor.demand());
    }
    
    @Test
    default void promisors_createLifeCyclePromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        final int usages = 5;
        final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
        when(referent.demand()).thenReturn(deliverable);
        when(deliverable.open()).thenReturn(deliverable);
        final Promisor<Decoy<Integer>> promisor = promisors.createLifeCyclePromisor(referent);
        
        assertNotNull(promisor, "should not return null.");
        
        for (int i = 0; i < usages; i++) {
            promisor.incrementUsage();
        }
        @SuppressWarnings("resource") final Decoy<Integer> actual = promisor.demand();
        for (int i = 0; i < usages; i++) {
            promisor.decrementUsage();
        }
        
        assertAll(
            () -> assertSame(deliverable, actual, "deliverables should match."),
            () -> verify(referent, times(usages)).decrementUsage(),
            () -> verify(referent, times(usages)).incrementUsage(),
            () -> verify(deliverable, never()).incrementUsage(),
            () -> verify(deliverable, never()).decrementUsage(),
            () -> verify(deliverable, times(1)).open(),
            () -> verify(deliverable, times(1)).close()
        );
    }
    
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 2, 3, 5, 8, 12, 17, 29 })
    default void lifeCyclePromisor_ClaimsDuringOpen(int threadCount) throws Throwable {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public void mockupDeliverable(Decoy<String> mockDeliverable) {
                overrideOpen(mockDeliverable, Duration.ofMillis(200), () -> {});
            }
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 2, 3, 5, 8, 12, 17, 29 })
    default void lifeCyclePromisor_ThrowingOpen(int threadCount) throws Throwable {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public float successPercentage() {
                return 0.f;
            }
            @Override
            public void mockupDeliverable(Decoy<String> mockDeliverable) {
                overrideOpen(mockDeliverable, Duration.ofMillis(0), () -> {
                    throw new IllegalStateException("Problem");
                });
            }
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 2, 3, 5, 8, 12, 17, 29 })
    default void lifeCyclePromisor_ClaimsDuringClose(int threadCount) throws Throwable  {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public void mockupDeliverable(Decoy<String> mockDeliverable) {
                overrideClose(mockDeliverable, Duration.ofMillis(100), () -> {});
            }
            @Override
            public void beforeWaitForCompletion(Promisor<Decoy<String>> testSubject) {
                sleep(Duration.ofMillis(100));
                testSubject.decrementUsage();
            }
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 2, 3, 5, 8, 12, 17, 29 })
    default void lifeCyclePromisor_ClaimsDuringDemand(int threadCount) throws Throwable {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public Duration demandDelay() {
                return Duration.ofMillis(100);
            }
        });
    }
    
    final class ConcurrencyTestsTool {
        private ConcurrencyTestsTool() {
        
        }
        interface ScenarioConfig {
            int threadCount();
            
            default void mockupDeliverable(Decoy<String> mockDeliverable) {
            }
            
            default void beforeWaitForCompletion(Promisor<Decoy<String>> testSubject) {
            }
            
            default Duration demandDelay() {
                return Duration.ZERO;
            }
            
            default int claimsPerThread() {
                return 123;
            }
            
            default float successPercentage() {
                return 1.0f;
            }
        }
        
        static void runWithScenario(ScenarioConfig config) throws Throwable {
            final int totalClaims = config.threadCount()*config.claimsPerThread();
            final int expectedSuccesses = (int)config.successPercentage()*totalClaims;
            final int expectedFailures = totalClaims-expectedSuccesses;
            final Contract<Decoy<String>> contract = Contract.create("Contract");
            final Thread[] claimThreads = new Thread[config.threadCount()];
            final AtomicInteger failedCount = new AtomicInteger(0);
            final AtomicInteger successCount = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(config.threadCount());
            final Decoy<String> mockDeliverable = spy();
            final Promisor<Decoy<String>> mockPromisor = spy();
            
            overrideDemand(mockPromisor, config.demandDelay(), () -> mockDeliverable);
            config.mockupDeliverable(mockDeliverable);
  
            final Promisor<Decoy<String>> testSubject = createTestSubject(mockPromisor);
            
            try (AutoClose closeBinding = bindContract(contract, testSubject)){
                nullCheck(closeBinding, "warning: [try] workaround");
                for (int i = 0; i < config.threadCount(); i++) {
                    claimThreads[i] = new Thread("Claim-" + i) {
                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i < config.claimsPerThread(); i++) {
                                    try {
                                        //noinspection resource
                                        Contracts.claimContract(contract);
                                        successCount.incrementAndGet();
                                    } catch (Throwable thrown) {
                                        failedCount.incrementAndGet();
                                    }
                                }
                            } finally {
                                latch.countDown();
                            }
                        }
                    };
                }
                for (int i = 0; i < config.threadCount(); i++) {
                    claimThreads[i].start();
                }
                config.beforeWaitForCompletion(testSubject);
                assertTrue(latch.await(1, TimeUnit.MINUTES), "Test took too long");
            }
            
            assertAll(
                () -> assertEquals(expectedSuccesses, successCount.get(), "Success count"),
                () -> assertEquals(expectedFailures, failedCount.get(), "Failed count")
            );
            if (expectedSuccesses > 0) {
               verify(mockDeliverable, times(1)).open();
               verify(mockDeliverable, times(1)).close();
            }
        }
        
        static <T> Promisor<T> createTestSubject(Promisor<T> promisor) {
            final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
            return promisors.createLifeCyclePromisor(promisor);
        }
   
        static <T> void overrideOpen(Decoy<T> decoy, Duration duration, Runnable block) {
            doAnswer((Answer<AutoClose>) invocation -> {
                sleep(duration);
                block.run();
                return decoy;
            }).when(decoy).open();
        }
        
        static <T> void overrideClose(Decoy<T> decoy, Duration duration, Runnable block) {
            doAnswer((Answer<Void>) invocation -> {
                sleep(duration);
                block.run();
                return null; // Void methods return null in doAnswer
            }).when(decoy).close();
        }
        
        static <T> void overrideDemand(Promisor<T> promisor, Duration duration, Supplier<T> block) {
            when(promisor.demand()).thenAnswer((Answer<T>) invocation -> {
                sleep(duration);
                return block.get();
            });
        }
    }
}
