package io.github.jonloucks.contracts.api;

/**
 * Opt-in interface to  For example, this is when threads should be stopped or hooks removed.
 * See also {@link AutoOpen}
 * Features like {@link Promisors#createLifeCyclePromisor(Promisor)}
 * will automatically call this method once if the deliverable implements this method.
 */
@FunctionalInterface
public interface AutoClose extends AutoCloseable {
    
    /**
     * For cases that need an AutoClose that does nothing
     */
    AutoClose NONE = () -> {};
    
    /**
     * AutoClose this instance.
     */
    void close();
}
