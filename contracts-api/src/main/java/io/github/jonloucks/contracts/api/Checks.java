package io.github.jonloucks.contracts.api;

public final class Checks {
    private Checks() {
        throw new AssertionError("Illegal constructor");
    }
    
    public static <T> Contract<T> contractCheck(Contract<T> contract) {
        return nullCheck(contract, "contract was null");
    }
    
    public static <T> Promisor<T> promisorCheck(Promisor<T> promisor) {
        return nullCheck(promisor, "promisor was null");
    }
    
    public static <T> T messageCheck(T t) {
        return nullCheck(t, "message was null");
    }

    public static <T> T nullCheck(T t, String message) {
        return illegalCheck(t, null == t, message);
    }
   
    public static <T> T illegalCheck(T t, boolean failed, String message) {
        if (failed) {
            final String checkedMessage = nullCheck(message, "message was null");
            throw new IllegalArgumentException(checkedMessage);
        }
        return t;
    }
}
