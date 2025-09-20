package io.github.jonloucks.contracts.api;

import java.util.function.Function;

/**
 * Helper methods for creating and chaining Promisors used for {@link Contracts#bindContract(Contract, Promisor)}
 */
public interface Promisors {
    
    /**
     * The contract for this interface. Singleton
     */
    Contract<Promisors> CONTRACT = Contract.create("Promisors");
    
    /**
     * Creates a Promisor that returns the given value every type it is claimed.
     *
     * @param deliverable the value to
     * @return The new Promisor
     * @param <T> the type of deliverable
     */
    <T> Promisor<T> createValuePromisor(T deliverable);
    
    /**
     * Creates a Promisor that only calls the source Promisor once and then always
     * returns that value.
     * Note: increment and decrementUsage are relayed to the source promisor.
     *
     * @param promisor the source Promisor
     * @return The new Promisor
     * @param <T> the type of deliverable
     */
    <T> Promisor<T> createSingletonPromisor(Promisor<T> promisor);
    
    /**
     * Reference counted, lazy loaded, with opt-in 'open' and 'close' invoked on deliverable.
     * Note: increment and decrementUsage are relayed to the source promisor.
     *
     * @param promisor the source promisor
     * @return the new Promisor
     * @param <T> the type of deliverable
     */
    <T> Promisor<T> createLifeCyclePromisor(Promisor<T> promisor);
    
    /**
     * Extract
     * Note: increment and decrementUsage are relayed to the source promisor.
     *
     * @param promisor the source promisor
     * @param extractor the function that gets an object from the deliverable. For example Person -> Age
     * @return the new Promisor
     * @param <T> the type of deliverable
     * @param <R> the new Promisor deliverable type
     */
    <T, R> Promisor<R> createExtractPromisor(Promisor<T> promisor, Function<T, R> extractor);
}
