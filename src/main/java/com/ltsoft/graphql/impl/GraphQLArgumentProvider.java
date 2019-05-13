package com.ltsoft.graphql.impl;

import com.google.common.base.CaseFormat;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.InstanceFactory;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GraphQLArgumentProvider extends BasicArgumentProvider<Object> {

    private static Cache<String, Optional<Method>> METHOD_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    private final InstanceFactory instanceFactory;

    public GraphQLArgumentProvider(Class<?> cls, Method method, Parameter parameter, InstanceFactory instanceFactory) {
        super(cls, method, parameter);
        this.instanceFactory = instanceFactory;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected Object toBean(Map<String, Object> source, TypeToken<?> type, DataFetchingEnvironment environment) {
        Object bean = instanceFactory.provide(type.getRawType());

        try {
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();

                if (val == null) {
                    continue;
                }

                Method method = findSetter(type, key, val);

                if (method != null) {
                    Object param = val;
                    TypeToken<?> paramType = TypeToken.of(method.getParameters()[0].getParameterizedType());

                    if (val instanceof Collection) {
                        param = toCollection(val, paramType, environment);
                    } else if (val instanceof Map && !paramType.isSupertypeOf(Map.class)) {
                        //noinspection unchecked
                        param = toBean((Map<String, Object>) val, paramType, environment);
                    }

                    method.invoke(bean, param);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(String.format("Convert data to %s fail", type.getRawType().getName()), e);
        }

        return bean;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Method findSetter(TypeToken<?> type, String key, Object val) {
        Class<?> rawType = type.getRawType();
        String methodName = "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, key);

        try {
            return METHOD_CACHE.get(rawType.getName() + "#" + key, () -> {
                try {
                    return Optional.of(rawType.getMethod(methodName, val.getClass()));
                } catch (NoSuchMethodException e) {
                    return Arrays.stream(rawType.getMethods())
                            .filter(method -> methodName.equals(method.getName()))
                            .findFirst();
                }
            }).orElse(null);
        } catch (ExecutionException e) {
            throw new IllegalStateException(String.format("Find method '%s' of class '%s' fail.", methodName, rawType.getName()), e);
        }
    }
}
