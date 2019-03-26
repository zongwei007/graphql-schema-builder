package com.ltsoft.graphql;

import graphql.schema.DataFetchingEnvironment;

@FunctionalInterface
public interface ArgumentProvider<T> {

    T provide(DataFetchingEnvironment environment);

}
