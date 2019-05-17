package com.ltsoft.graphql.impl;

import com.google.common.base.CaseFormat;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ArgumentConverter;
import com.ltsoft.graphql.InstanceFactory;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GraphQLArgumentProvider extends BasicArgumentProvider<Object> {

    private static Cache<String, Optional<Method>> METHOD_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    private final InstanceFactory instanceFactory;
    private final List<ArgumentConverter<?>> converters;

    public GraphQLArgumentProvider(Class<?> cls, Method method, Parameter parameter, InstanceFactory instanceFactory, List<ArgumentConverter<?>> converters) {
        super(cls, method, parameter);
        this.instanceFactory = instanceFactory;
        this.converters = converters;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected <E> E toBean(Map<String, Object> source, TypeToken<E> type, DataFetchingEnvironment environment) {
        Optional<ArgumentConverter<?>> converter = converters.stream()
                .filter(ele -> ele.isSupport(type.getRawType()))
                .findFirst();

        if (converter.isPresent()) {
            //noinspection unchecked
            return converter.map(ele -> (E) ele.convert(source, type.getRawType())).orElse(null);
        }

        Object bean = instanceFactory.provide(type.getRawType());

        try {
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();

                Method method = findSetter(type, key, val);

                if (method == null) {
                    continue;
                }

                TypeToken<?> paramType = TypeToken.of(method.getParameters()[0].getParameterizedType());

                method.invoke(bean, toTypeOf(val, paramType, environment));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(String.format("Convert data to %s fail", type.getRawType().getName()), e);
        }

        //noinspection unchecked
        return (E) bean;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Method findSetter(TypeToken<?> type, String key, Object val) {
        Class<?> rawType = type.getRawType();
        String methodName = "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, key);

        try {
            return METHOD_CACHE.get(rawType.getName() + "#" + key, () -> {
                try {
                    if (val != null) {
                        return Optional.of(rawType.getMethod(methodName, val.getClass()));
                    } else {
                        return findSetterByName(rawType, methodName);
                    }
                } catch (NoSuchMethodException e) {
                    return findSetterByName(rawType, methodName);
                }
            }).orElse(null);
        } catch (ExecutionException e) {
            throw new IllegalStateException(String.format("Find method '%s' of class '%s' fail.", methodName, rawType.getName()), e);
        }
    }

    private Optional<Method> findSetterByName(Class<?> type, String methodName) {
        return Arrays.stream(type.getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst();
    }
}
