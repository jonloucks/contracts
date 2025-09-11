package io.github.jonloucks.contracts.api;

/**
 * Opt-in interface to be called once after creation, although implementations should
 * handle this gracefully. For example, this is when threads or hooks could be added.
 * See also {@link Shutdown}
 * Features like {@link Promisors#createLifeCyclePromisor(Promisor)}
 * will automatically call this method once if the deliverable implements this method.
 */
@FunctionalInterface
public interface Startup {
    
    /**
     * Startup this instance
     */
    void startup();
}
