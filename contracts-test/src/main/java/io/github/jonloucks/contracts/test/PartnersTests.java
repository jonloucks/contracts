package io.github.jonloucks.contracts.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.ContractException;
import io.github.jonloucks.contracts.api.Contracts;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.jonloucks.contracts.test.Tools.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

public interface PartnersTests {
    
    @Test
    default void partners_shadowPartners_Works() {
        withContracts(partner -> {
            final Contracts.Config config = new Contracts.Config() {
                @Override
                public List<Contracts> getPartners() {
                    return singletonList(partner);
                }
            };
            withContracts(config, primary -> {
                final Contract<String> contract = Contract.create(String.class);
                
                try (AutoClose close1 = partner.bind(contract, () -> "Partner");
                     AutoClose close2 = primary.bind(contract, () -> "Primary")) {
                    ignore(close1);
                    ignore(close2);
                    
                    assertEquals("Primary", primary.claim(contract));
                }
            });
        });
    }
    
    @Test
    default void partners_unboundContracts_Works() {
        withContracts(partner -> {
            final Contracts.Config config = new Contracts.Config() {
                @Override
                public List<Contracts> getPartners() {
                    return singletonList(partner);
                }
            };
            withContracts(config, primary -> {
                final Contract<String> contract = Contract.create(String.class);
                
                assertFalse(primary.isBound(contract));
                assertThrown(assertThrows(ContractException.class, () -> primary.claim(contract)));
            });
        });
    }
    
    @Test
    default void partners_boundPartners_Works() {
        withContracts(partner -> {
            final Contracts.Config config = new Contracts.Config() {
                @Override
                public List<Contracts> getPartners() {
                    return singletonList(partner);
                }
            };
            withContracts(config, primary -> {
                final Contract<String> contract = Contract.create(String.class);
                
                try (AutoClose close1 = partner.bind(contract, () -> "Partner")) {
                    ignore(close1);
                    
                    assertTrue(primary.isBound(contract));
                    assertEquals("Partner", primary.claim(contract));
                }
            });
        });
    }
    
    @Test
    default void partners_boundPrimary_Works() {
        withContracts(partner -> {
            final Contracts.Config config = new Contracts.Config() {
                @Override
                public List<Contracts> getPartners() {
                    return singletonList(partner);
                }
            };
            withContracts(config, primary -> {
                final Contract<String> contract = Contract.create(String.class);
                
                try (AutoClose close = primary.bind(contract, () -> "Primary")) {
                    ignore(close);
                    
                    assertTrue(primary.isBound(contract));
                    assertEquals("Primary", primary.claim(contract));
                }
            });
        });
    }
}
