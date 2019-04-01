package com.ltsoft.graphql.impl;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ArgumentProvider;
import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.annotations.GraphQLTypeReference;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;

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
            return toBean(environment.getArguments(), typeToken, environment);
        }

        Object value = environment.getArgument(argumentName);

        if (value instanceof Collection) {
            return toCollection(value, typeToken, environment);
        } else if (value instanceof Map) {
            if (typeToken.isSubtypeOf(Map.class)) {
                return toMap(value, typeToken);
            } else {
                //noinspection unchecked
                return toBean((Map<String, Object>) value, typeToken, environment);
            }
        } else {
            //noinspection unchecked
            return (T) value;
        }
    }

    @SuppressWarnings({"unchecked"})
    protected T toMap(Object value, TypeToken<?> typeToken) {
        checkArgument(value instanceof Map);

        if (typeToken.isSupertypeOf(value.getClass())) {
            return (T) value;
        } else {
            try {
                Map<String, Object> source = (Map<String, Object>) value;
                Map<String, Object> target;

                if (typeToken.getRawType().equals(Map.class)) {
                    target = new HashMap<>();
                } else {
                    target = (Map<String, Object>) typeToken.getRawType().getConstructor().newInstance();
                }

                target.putAll(source);

                return (T) target;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    protected T toCollection(Object value, TypeToken<?> typeToken, DataFetchingEnvironment environment) {
        checkArgument(value instanceof Collection);

        boolean isArray = typeToken.isArray();

        TypeToken<?> rawType;
        Class<?> listType = null;

        if (typeToken.getComponentType() != null) {
            rawType = typeToken.getComponentType();
        } else if (typeToken.isSubtypeOf(Iterable.class)) {
            listType = typeToken.getRawType();
            rawType = typeToken.resolveType(listType.getTypeParameters()[0]);
        } else {
            throw new IllegalArgumentException(String.format("Can not resolve collection type of '%s'", typeToken));
        }

        Stream<?> resultStream = ((Collection<?>) value).stream()
                .map(ele -> {
                    if (ele instanceof Map) {
                        if (rawType.isSubtypeOf(Map.class)) {
                            return toMap(ele, rawType);
                        } else {
                            //noinspection unchecked
                            return toBean((Map<String, Object>) ele, rawType, environment);
                        }
                    } else {
                        return ele;
                    }
                });

        //noinspection unchecked
        return (T) (isArray ? resultStream.toArray() : resultStream.collect(Collectors.toCollection(new CollectionSupplier(listType))));
    }

    protected abstract T toBean(Map<String, Object> source, TypeToken<?> type, DataFetchingEnvironment environment);

    private static class CollectionSupplier implements Supplier<Collection> {

        private final Class<?> listType;

        CollectionSupplier(Class<?> listType) {
            this.listType = listType;
        }

        @Override
        public Collection get() {
            if (listType.equals(Set.class)) {
                return new HashSet<>();
            } else if (listType.equals(Iterable.class) || listType.equals(Collection.class) || listType.equals(List.class)) {
                return new ArrayList<>();
            } else {
                try {
                    return (Collection) listType.getConstructor().newInstance();
                } catch (ClassCastException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
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
