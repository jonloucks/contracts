package io.github.jonloucks.contracts.api;

import java.util.Optional;

import static io.github.jonloucks.contracts.api.BindStrategy.ALWAYS;
import static io.github.jonloucks.contracts.api.PunchBoard.SERVICE_FINDER;

@SuppressWarnings("unused") // used if jave version >= 9
final class Punch_Java_9 {
    Punch_Java_9() {
        punchServiceFinder();
    }
    
    private void punchServiceFinder() {
        PunchBoard.setPunch(SERVICE_FINDER, () -> new PunchBoard.ServiceFinder() {
            @Override
            public <S> Optional<S> findInstance(Class<S> service) {
                try {
                    return java.util.ServiceLoader.load(service).findFirst();
                } catch (Throwable ignored) {
                    return Optional.empty();
                }
            }
        }, ALWAYS);
    }
}
