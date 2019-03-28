package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import graphql.schema.idl.EnumValuesProvider;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveFieldName;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class EnumFieldValueProvider implements EnumValuesProvider {

    private final String typeName;
    private final Map<String, Object> valueMap;

    public EnumFieldValueProvider(Class<?> type) {
        this.typeName = resolveTypeName(type.isAnnotationPresent(GraphQLTypeExtension.class) ? type.getAnnotation(GraphQLTypeExtension.class).value() : type);
        this.valueMap = resolveValueMap(type);
    }

    private Map<String, Object> resolveValueMap(Class<?> type) {
        Map<String, Object> result = new HashMap<>();
        Object[] constants = type.getEnumConstants();
        Field[] fields = type.getFields();

        for (int i = 0, len = constants.length; i < len; i++) {
            result.put(resolveFieldName(null, fields[i]), constants[i]);
        }

        return result;
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
