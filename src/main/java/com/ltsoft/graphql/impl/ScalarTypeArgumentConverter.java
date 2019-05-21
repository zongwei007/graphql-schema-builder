package com.ltsoft.graphql.impl;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ArgumentConverter;
import com.ltsoft.graphql.scalars.ScalarTypeRepository;

import java.util.function.BiFunction;

@SuppressWarnings("UnstableApiUsage")
public class ScalarTypeArgumentConverter implements ArgumentConverter<Object> {

    @Override
    public Boolean isSupport(TypeToken<?> from, TypeToken<?> to) {
        return ScalarTypeRepository.getInstance()
                .findMappingScalarType(to.getRawType())
                .isPresent();
    }

    @Override
    public Object convert(Object source, TypeToken<Object> type, BiFunction<Object, TypeToken<?>, ?> orElse) {
        return ScalarTypeRepository.getInstance()
                .findMappingScalarType(type.getRawType())
                .map(scalarType -> scalarType.getCoercing().parseValue(source))
                .orElseGet(() -> orElse.apply(source, type));
    }
}
