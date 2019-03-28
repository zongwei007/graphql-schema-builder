package com.ltsoft.graphql;

import graphql.schema.DataFetcher;

/**
 * GraphQL 默认 DataFetcher 工厂类
 *
 * @param <T> DataFetcher 返回类型
 */
public interface DefaultDataFetcherFactory<T> {

    /**
     * 生成一个 {@link graphql.schema.DataFetcher}
     *
     * @param typeName 类型名称
     * @return DataFetcher
     */
    DataFetcher<T> get(String typeName);

}
