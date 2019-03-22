package com.ltsoft.graphql;

import graphql.schema.DataFetchingEnvironment;

import java.util.function.Function;

public interface ArgumentProvider<R> extends Function<DataFetchingEnvironment, R> {

}
