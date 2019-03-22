package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.ArgumentProvider;
import com.ltsoft.graphql.resolver.ResolveUtil;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Parameter;

public class GraphQLArgumentProvider implements ArgumentProvider<Object> {

    private final String argumentName;

    public GraphQLArgumentProvider(Parameter parameter) {
        this.argumentName = ResolveUtil.resolveArgumentName(parameter);
    }

    @Override
    public Object apply(DataFetchingEnvironment dataFetchingEnvironment) {
        return dataFetchingEnvironment.getArgument(argumentName);
    }
}
