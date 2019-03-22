package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.ArgumentProvider;
import graphql.schema.DataFetchingEnvironment;

public class GraphQLDataFetchingEnvironmentProvider implements ArgumentProvider<DataFetchingEnvironment> {
    @Override
    public DataFetchingEnvironment apply(DataFetchingEnvironment environment) {
        return environment;
    }
}
