package com.ltsoft.graphql.impl;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ArgumentProvider;
import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.annotations.GraphQLTypeReference;
import com.ltsoft.graphql.scalars.ScalarTypeRepository;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings({"UnstableApiUsage", "WeakerAccess"})
public abstract class BasicArgumentProvider<T> implements ArgumentProvider<T> {

    private final String argumentName;
    private final TypeToken<?> typeToken;
    private boolean isGenericType;

    public BasicArgumentProvider(Class<?> cls, Method method, Parameter parameter) {
        checkArgument(parameter.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLArgument.class));

        this.typeToken = resolveGenericType(cls, parameter.getParameterizedType());
        this.argumentName = resolveArgumentName(parameter);

        Class<?> rawType = typeToken.getRawType();
        //非 List 类型，且未声明类型引用
        if (!isGraphQLList(typeToken) && !parameter.isAnnotationPresent(GraphQLTypeReference.class)) {
            //非枚举且直接定义为 GraphQLType 的参数，才能识别为泛参数
            if (!rawType.isEnum() && rawType.isAnnotationPresent(GraphQLType.class)) {
                this.isGenericType = true;
            }
        }
    }

    @Override
    public T provide(DataFetchingEnvironment environment) {
        if (isGenericType) {
            //noinspection unchecked
            return (T) toBean(environment.getArguments(), typeToken, environment);
        }

        //noinspection unchecked
        return (T) toTypeOf(environment.getArgument(requireNonNull(argumentName)), typeToken, environment);
    }

    protected abstract <E> E toBean(Map<String, Object> source, TypeToken<E> type, DataFetchingEnvironment environment);

    @SuppressWarnings("unchecked")
    protected <E, I> E toTypeOf(Object value, TypeToken<E> typeToken, DataFetchingEnvironment environment) {
        if (value == null) {
            return null;
        }

        if (!typeToken.isSupertypeOf(value.getClass()) && isScalarType(typeToken)) {
            return toScalarType(value, typeToken);
        }

        if (value instanceof Collection) {
            if (typeToken.isArray()) {
                return (E) toArray(value, unwrapListType(typeToken), environment);
            } else {
                return (E) toCollection(value, (TypeToken<Collection<I>>) typeToken, environment);
            }
        }

        if (value instanceof Map) {
            return toBean((Map<String, Object>) value, typeToken, environment);
        }

        return (E) value;
    }

    protected <I> I[] toArray(Object value, TypeToken<I> itemType, DataFetchingEnvironment environment) {
        //noinspection unchecked
        return toStream(value, itemType, environment)
                .toArray(len -> (I[]) Array.newInstance(itemType.getRawType(), len));
    }

    protected <I> Collection<I> toCollection(Object value, TypeToken<? extends Collection<I>> listType, DataFetchingEnvironment environment) {
        TypeToken<?> itemType = unwrapListType(listType);

        //noinspection unchecked
        return (Collection<I>) toStream(value, itemType, environment)
                .collect(Collectors.toCollection(new CollectionSupplier(listType.getRawType())));
    }

    private <I> Stream<I> toStream(Object value, TypeToken<I> itemType, DataFetchingEnvironment environment) {
        checkArgument(value instanceof Collection);

        return ((Collection<?>) value).stream()
                .map(ele -> toTypeOf(ele, itemType, environment));
    }

    protected static boolean isScalarType(TypeToken<?> typeToken) {
        return ScalarTypeRepository.getInstance()
                .findMappingScalarType(typeToken.getRawType())
                .isPresent();
    }

    protected static <E> E toScalarType(Object source, TypeToken<E> typeToken) {
        //noinspection unchecked
        return ScalarTypeRepository.getInstance()
                .findMappingScalarType(typeToken.getRawType())
                .map(scalarType -> (E) scalarType.getCoercing().parseValue(source))
                .orElseThrow(() -> new IllegalArgumentException(String.format("'%s' is not a registered scalar type.", typeToken.toString())));
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

    public String getArgumentName() {
        return argumentName;
    }

    public boolean isGenericType() {
        return isGenericType;
    }
}
