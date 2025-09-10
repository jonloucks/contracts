package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Contracts.bindContract;
import static io.github.jonloucks.contracts.test.LifeCyclePromisorTests.ConcurrencyTestsTool.*;
import static io.github.jonloucks.contracts.test.Tools.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public interface LifeCyclePromisorTests {
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 2, 3, 5, 8, 12, 17, 29 })
    default void lifeCyclePromisor_ClaimsDuringStartup(int threadCount) throws Throwable {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public void mockupDeliverable(Decoy<String> mockDeliverable) {
                overrideStartup(mockDeliverable, Duration.ofMillis(200), () -> {});
            }
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 2, 3, 5, 8, 12, 17, 29 })
    default void lifeCyclePromisor_ThrowingStartup(int threadCount) throws Throwable {
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
                overrideStartup(mockDeliverable, Duration.ofMillis(0), () -> {
                    throw new IllegalStateException("Problem");
                });
            }
        });
    }
    
    @ParameterizedTest(name = "Threads = {0}")
    @ValueSource(ints = { 1, 2, 3, 5, 8, 12, 17, 29 })
    default void lifeCyclePromisor_ClaimsDuringShutdown(int threadCount) throws Throwable  {
        runWithScenario(new ScenarioConfig() {
            @Override
            public int threadCount() {
                return threadCount;
            }
            @Override
            public void mockupDeliverable(Decoy<String> mockDeliverable) {
                overrideShutdown(mockDeliverable, Duration.ofMillis(100), () -> {});
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
            
            final Shutdown unbind = bindContract(contract, testSubject);
            try {
                for (int i = 0; i < config.threadCount(); i++) {
                    claimThreads[i] = new Thread("Claim-" + i) {
                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i < config.claimsPerThread(); i++) {
                                    try {
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
            } finally {
                unbind.shutdown();
            }
            
            assertAll(
                () -> assertEquals(expectedSuccesses, successCount.get(), "Success count"),
                () -> assertEquals(expectedFailures, failedCount.get(), "Failed count")
            );
            if (expectedSuccesses > 0) {
               verify(mockDeliverable, times(1)).startup();
               verify(mockDeliverable, times(1)).shutdown();
            }
        }
        
        static <T> Promisor<T> createTestSubject(Promisor<T> promisor) {
            final Promisors promisors = Contracts.claimContract(Promisors.CONTRACT);
            return promisors.createLifeCyclePromisor(promisor);
        }
   
        static <T> void overrideStartup(Decoy<T> decoy, Duration duration, Runnable block) {
            doAnswer((Answer<Void>) invocation -> {
                sleep(duration);
                block.run();
                return null; // Void methods return null in doAnswer
            }).when(decoy).startup();
        }
        
        static <T> void overrideShutdown(Decoy<T> decoy, Duration duration, Runnable block) {
            doAnswer((Answer<Void>) invocation -> {
                sleep(duration);
                block.run();
                return null; // Void methods return null in doAnswer
            }).when(decoy).shutdown();
        }
        
        static <T> void overrideDemand(Promisor<T> promisor, Duration duration, Supplier<T> block) {
            when(promisor.demand()).thenAnswer((Answer<T>) invocation -> {
                sleep(duration);
                return block.get();
            });
        }
    }
}
