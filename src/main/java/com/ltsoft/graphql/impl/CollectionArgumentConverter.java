package com.ltsoft.graphql.impl;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ArgumentConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.unwrapListType;

@SuppressWarnings("UnstableApiUsage")
public class CollectionArgumentConverter implements ArgumentConverter<Collection> {

    @Override
    public Boolean isSupport(TypeToken<?> from, TypeToken<?> to) {
        return from.isSubtypeOf(Collection.class) && to.isSubtypeOf(Collection.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<?> convert(Object source, TypeToken<Collection> type, BiFunction<Object, TypeToken<?>, ?> orElse) {
        checkArgument(source instanceof Collection);

        TypeToken<?> itemType = unwrapListType(type);

        Class<Collection> rawType = (Class<Collection>) type.getRawType();

        return ((Collection<?>) source).stream()
                .map(ele -> orElse.apply(ele, itemType))
                .collect(Collectors.toCollection(new CollectionSupplier<>(rawType)));
    }

    private static class CollectionSupplier<T extends Collection> implements Supplier<T> {

        private final Class<T> listType;

        CollectionSupplier(Class<T> listType) {
            this.listType = listType;
        }

        @Override
        public T get() {
            if (listType.equals(Set.class)) {
                //noinspection unchecked
                return (T) new HashSet();
            } else if (listType.equals(Collection.class) || listType.equals(List.class)) {
                //noinspection unchecked
                return (T) new ArrayList();
            } else {
                try {
                    return listType.getConstructor().newInstance();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalArgumentException(String.format("Can not build type '%s' as Collection", listType.getName()), e);
                }
            }
        }
    }
}
