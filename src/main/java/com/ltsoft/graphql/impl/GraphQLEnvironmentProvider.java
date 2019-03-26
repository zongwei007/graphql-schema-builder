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
    public Object provide(DataFetchingEnvironment environment) {
        try {
            return method.invoke(environment);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method resolveMethod(String propName) {
        String methodName = "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, propName);

        try {
            return DataFetchingEnvironment.class.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Can not find getter method '%s' from DataFetchingEnvironment", methodName), e);
        }
    }
}
