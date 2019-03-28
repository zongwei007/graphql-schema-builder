package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.DefaultDataFetcherFactory;
import graphql.schema.DataFetcher;

public class EmptyDataFetcherFactory implements DefaultDataFetcherFactory {

    @Override
    public DataFetcher get(String typeName) {
        return environment -> null;
    }
}
