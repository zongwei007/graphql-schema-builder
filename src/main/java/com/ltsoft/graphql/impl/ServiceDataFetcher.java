package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.ArgumentProvider;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Method;
import java.util.List;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveFieldName;

public class ServiceDataFetcher implements DataFetcher {

    private final String fieldName;
    private final Object instance;
    private final Method method;
    private final List<ArgumentProvider<?>> providers;

    public ServiceDataFetcher(Object instance, Method method, List<ArgumentProvider<?>> providers) {
        this.fieldName = resolveFieldName(instance.getClass(), method, null);
        this.instance = instance;
        this.method = method;
        this.providers = providers;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        return method.invoke(instance, providers.stream().map(ele -> ele.provide(environment)).toArray());
    }
}
