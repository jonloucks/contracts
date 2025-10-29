package io.github.jonloucks.contracts.api;

import static io.github.jonloucks.contracts.api.BindStrategy.IF_NOT_BOUND;
import static io.github.jonloucks.contracts.api.PunchBoard.*;

@SuppressWarnings("unused")
final class Punch_Defaults {
    Punch_Defaults() {
        punchContractsConfig();
    }
    
    private void punchContractsConfig() {
        PunchBoard.setPunch(GLOBAL_CONFIG, () -> new Contracts.Config() {
                @Override
                public boolean useServiceLoader() {
                    return false;
                }
            }
            , IF_NOT_BOUND);
    }
}
