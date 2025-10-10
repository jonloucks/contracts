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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.test.LifeCyclePromisorTests.ConcurrencyTestsTool.*;
import static io.github.jonloucks.contracts.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"Convert2MethodRef"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface LifeCyclePromisorTests {
    
    @Test
    default void lifeCyclePromisor_WithNullPromisor_Throws() {
        withContracts(contracts -> {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                promisors.createLifeCyclePromisor(null)
            );
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void lifeCyclePromisor_demand_WithoutUsage_Throws() {
        withContracts(contracts -> {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            final Promisor<String> promisor = promisors.createLifeCyclePromisor(() -> "abc");
            
            final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
                promisor.demand();
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void lifeCyclePromisor_WithNullDeliverable_IsAllowed() {
        withContracts(contracts -> {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
            final Promisor<String> promisor = promisors.createLifeCyclePromisor(()->null);
            promisor.incrementUsage();
            
            assertNotNull(promisor, "should not return null.");
            assertNull(promisor.demand());
        });
    }
    
    @Test
    default void lifeCyclePromisor_Valid_Works(@Mock Promisor<Decoy<Integer>> referent, @Mock Decoy<Integer> deliverable) {
        withContracts(contracts -> {
            final int usages = 5;
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
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
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 3, 29 })
    default void lifeCyclePromisor_ClaimsDuringOpen(int threadCount) throws Throwable {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public void mockupDeliverable(Decoy<String> mockDeliverable) {
                overrideOpen(mockDeliverable, Duration.ofMillis(100), () -> {});
            }
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 3, 29 })
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
                    switch (threadCount%3) {
                        case 0:
                            throw new RuntimeException("RuntimeException");
                        case 1:
                            throw new Error("Error");
                        case 2:
                            throw new ArithmeticException("Error");
                    }
                });
            }
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 3, 29 })
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
    @ValueSource(ints = { 1, 3, 29 })
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
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 3, 29 })
    default void lifeCyclePromisor_Demand_Reentrancy_Works(int threadCount) throws Throwable {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public boolean reentrancy() {
                return true;
            }
        });
    }
    
    @Test
    default void lifecyclePromisor_InternalCoverage() {
        assertInstantiateThrows(ConcurrencyTestsTool.class);
    }
    
    final class ConcurrencyTestsTool {
        private ConcurrencyTestsTool() {
            throw new AssertionError("Illegal constructor");
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
            
            @SuppressWarnings("SameReturnValue")
            default int claimsPerThread() {
                return 123;
            }
            
            default float successPercentage() {
                return 1.0f;
            }
            
            default boolean reentrancy() {
                return false;
            }
            
        }
        
        static void runWithScenario(ScenarioConfig config) {
            final int totalClaims = config.threadCount()*config.claimsPerThread();
            final int expectedSuccesses = (int)config.successPercentage()*totalClaims;
            final int expectedFailures = totalClaims-expectedSuccesses;
            final Contract<Decoy<String>> contract = Contract.create("Contract");
            final Thread[] claimThreads = new Thread[config.threadCount()];
            final AtomicInteger failedCount = new AtomicInteger(0);
            final AtomicInteger successCount = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(config.threadCount());
            @SuppressWarnings("resource") final Decoy<String> mockDeliverable = spy();
            final Promisor<Decoy<String>> mockPromisor = spy();
            final AtomicBoolean firstDemand = new AtomicBoolean(true);
            
            withContracts( contracts -> {
                config.mockupDeliverable(mockDeliverable);
                overrideDemand(mockPromisor, config.demandDelay(), () -> {
                    if (firstDemand.compareAndSet(true, false)) {
                        config.demandDelay();
                    } else if (config.reentrancy()) {
                        return contracts.claim(contract);
                    }
                    return mockDeliverable;
                });
                
                final Promisor<Decoy<String>> testSubject = createTestSubject(contracts, mockPromisor);
                
                try (AutoClose closeBinding = contracts.bind(contract, testSubject)) {
                    final AutoClose ignored2 = closeBinding;
                    for (int i = 0; i < config.threadCount(); i++) {
                        claimThreads[i] = new Thread("Claim-" + i) {
                            @Override
                            public void run() {
                                try {
                                    for (int i = 0; i < config.claimsPerThread(); i++) {
                                        try {
                                            //noinspection resource
                                            contracts.claim(contract);
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
                    try {
                        assertTrue(latch.await(1, TimeUnit.MINUTES), "Test took too long");
                    } catch (InterruptedException e) {
                        fail("Test took too long");
                    }
                }
                
                assertAll(
                    () -> assertEquals(expectedSuccesses, successCount.get(), "Success count"),
                    () -> assertEquals(expectedFailures, failedCount.get(), "Failed count")
                );
                if (expectedSuccesses > 0) {
                    verify(mockDeliverable, times(1)).open();
                    verify(mockDeliverable, times(1)).close();
                }
            });
        }
        
        static <T> Promisor<T> createTestSubject(Contracts contracts, Promisor<T> promisor) {
            final Promisors promisors = contracts.claim(Promisors.CONTRACT);
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
        
        private static <T> void overrideDemand(Promisor<T> promisor, Duration duration, Supplier<T> block) {
            when(promisor.demand()).thenAnswer((Answer<T>) invocation -> {
                sleep(duration);
                return block.get();
            });
        }
    }
}
