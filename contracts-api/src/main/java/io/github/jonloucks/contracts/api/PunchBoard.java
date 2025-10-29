package io.github.jonloucks.contracts.api;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.contractCheck;
import static java.util.Optional.ofNullable;

final class PunchBoard {
    
    @FunctionalInterface
    interface ServiceFinder {
        <S> Optional<S> findInstance(Class<S> service);
    }
    
    static final Contract<ServiceFinder> SERVICE_FINDER = Contract.create(ServiceFinder.class);
    
    static final Contract<Contracts.Config> GLOBAL_CONFIG = Contract.create("GlobalConfig");
    
    static <T> void setPunch(Contract<T> contract, Supplier<T> value, BindStrategy bindStrategy) {
        final Contract<T> validContract = contractCheck(contract);
        if (ofNullable(value).isPresent()) {
            board.put(validContract, value);
        } else {
            board.remove(validContract);
        }
    }
    
    static <T> Optional<T> getPunch(Contract<T> contract) {
        final Contract<T> validContract = contractCheck(contract);
        final Optional<Supplier<?>> optionalFactory = ofNullable(board.get(contract));
        return optionalFactory.map(supplier -> validContract.cast(supplier.get()));
    }
    
     static void loadPunch(String name) {
        try {
            // only classes in this package
            final String packageName = PunchBoard.class.getPackage().getName();
            final String className = packageName + ".Punch_" + name;
            final Constructor<?> constructor = Class.forName(className).getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Throwable ignored) {
        }
    }
    
    static int getJavaVersion() {
        return JAVA_VERSION;
    }
    
    private static int initJavaVersion() {
        try {
            final String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
                return Integer.parseInt(version.substring(2, 3));
            } else {
                int dot = version.indexOf(".");
                if (dot == -1) {
                    return Integer.parseInt(version);
                } else {
                    return Integer.parseInt(version.substring(0, dot));
                }
            }
        } catch (Throwable ignored) {
            return 8;
        }
    }
    
    private PunchBoard() {
    }
    
    private static final ConcurrentHashMap<Contract<?>, Supplier<?>> board = new ConcurrentHashMap<>();
    private static final int JAVA_VERSION;
    
    static {
        JAVA_VERSION = initJavaVersion();
        loadPunch("Defaults");
        for (int n = 8; n <= getJavaVersion(); n += 1) {
            loadPunch("Java_" + n);
        }
    }
}
