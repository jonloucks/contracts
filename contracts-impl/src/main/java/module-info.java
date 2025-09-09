import io.github.jonloucks.contracts.api.ServiceFactory;
import io.github.jonloucks.contracts.impl.ServiceFactoryImpl;

module io.github.jonloucks.contracts.impl {
    requires io.github.jonloucks.contracts.api;
    
    opens io.github.jonloucks.contracts.impl to io.github.jonloucks.contracts.api;
    
    provides ServiceFactory with ServiceFactoryImpl;
}