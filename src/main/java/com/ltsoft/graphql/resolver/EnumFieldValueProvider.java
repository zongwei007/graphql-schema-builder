package com.ltsoft.graphql.resolver;

import graphql.schema.idl.EnumValuesProvider;

import java.util.Map;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveEnumValueMap;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class EnumFieldValueProvider implements EnumValuesProvider {

    private final String typeName;
    private final Map<String, Object> valueMap;

    public EnumFieldValueProvider(Class<?> type) {
        this.typeName = resolveTypeName(type);
        this.valueMap = resolveEnumValueMap(type);
    }

    public EnumFieldValueProvider merge(EnumFieldValueProvider other) {
        other.valueMap.forEach(this.valueMap::putIfAbsent);
        return this;
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public Object getValue(String name) {
        return valueMap.get(name);
    }
}
