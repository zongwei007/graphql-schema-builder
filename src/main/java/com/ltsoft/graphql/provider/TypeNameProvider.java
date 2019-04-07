package com.ltsoft.graphql.provider;

import com.ltsoft.graphql.TypeProvider;
import graphql.language.Definition;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class TypeNameProvider implements TypeProvider<Definition> {

    private final String typeName;

    public TypeNameProvider(Class<?> cls) {
        this.typeName = resolveTypeName(cls);
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public Definition<Definition> getDefinition() {
        throw new IllegalAccessError("TypeNameProvider is not support to resolve definition information");
    }
}
