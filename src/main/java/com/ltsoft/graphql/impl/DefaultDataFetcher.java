package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.ArgumentProvider;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Method;
import java.util.List;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveFieldName;

public class DefaultDataFetcher implements DataFetcher {

    private final Object instance;
    private final Method method;
    private final List<ArgumentProvider<?>> providers;

    public DefaultDataFetcher(Method method, Object instance, List<ArgumentProvider<?>> providers) {
        this.instance = instance;
        this.method = method;
        this.providers = providers;
    }

    public String getFieldName() {
        return resolveFieldName(method, null);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        return method.invoke(instance, providers.stream().map(ele -> ele.apply(environment)).toArray());
    }
}
