package io.github.jonloucks.contracts.api;

/**
 * Opt-in interface to  For example, this is when threads should be stopped or hooks removed.
 * See also {@link Startup}
 * Features like {@link Promisors#createLifeCyclePromisor(Promisor)}
 * will automatically call this method once if the deliverable implements this method.
 */
@FunctionalInterface
public interface Shutdown {
    
    /**
     * Shutdown this instance.
     */
    void shutdown();
}
