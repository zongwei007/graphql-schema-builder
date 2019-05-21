package com.ltsoft.graphql.impl;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ArgumentConverter;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.unwrapListType;

@SuppressWarnings("UnstableApiUsage")
public class ArrayArgumentConverter implements ArgumentConverter<Object[]> {

    @Override
    public Boolean isSupport(TypeToken<?> from, TypeToken<?> to) {
        return from.isSubtypeOf(Collection.class) && to.isArray();
    }

    @Override
    public Object[] convert(Object source, TypeToken<Object[]> type, BiFunction<Object, TypeToken<?>, ?> orElse) {
        checkArgument(source instanceof Collection);

        TypeToken<?> itemType = unwrapListType(type);

        return ((Collection<?>) source).stream()
                .map(ele -> orElse.apply(ele, itemType))
                .toArray(len -> (Object[]) Array.newInstance(itemType.getRawType(), len));
    }
}
