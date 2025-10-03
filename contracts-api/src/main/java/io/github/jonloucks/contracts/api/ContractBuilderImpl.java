package io.github.jonloucks.contracts.api;

import static io.github.jonloucks.contracts.api.Checks.nameCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class ContractBuilderImpl<T> implements Contract.Config.Builder<T> {
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public String typeName() {
        return typeName;
    }
    
    @Override
    public boolean isReplaceable() {
        return replaceable;
    }

    @Override
    public Builder<T> name(String name) {
        this.name = nameCheck(name);
        return this;
    }
    
    @Override
    public Builder<T> typeName(String typeName) {
        this.typeName = nullCheck(typeName, "typeName was not present");
        return this;
    }
    
    @Override
    public Builder<T> replaceable(boolean replaceable) {
        this.replaceable = replaceable;
        return this;
    }
    
    @Override
    public T cast(Object instance) {
        return type.cast(instance);
    }
    
    ContractBuilderImpl(Class<T> type) {
        this.type = type;
        this.name = this.typeName = type.getTypeName();
    }
    
    private String name;
    private String typeName;
    private boolean replaceable;
    private final Class<T> type;
}
