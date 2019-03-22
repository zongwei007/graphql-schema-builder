package com.ltsoft.graphql.impl;

import com.google.common.base.CaseFormat;
import com.ltsoft.graphql.ArgumentProvider;
import com.ltsoft.graphql.annotations.GraphQLEnvironment;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GraphQLEnvironmentProvider implements ArgumentProvider<Object> {

    private final Method method;

    public GraphQLEnvironmentProvider(GraphQLEnvironment annotation) {
        this.method = resolveMethod(annotation.value());
    }

    @Override
    public Object apply(DataFetchingEnvironment environment) {
        try {
            return method.invoke(environment);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method resolveMethod(String propName) {
        try {
            return DataFetchingEnvironment.class.getMethod("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, propName));
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("");
        }
    }
}
