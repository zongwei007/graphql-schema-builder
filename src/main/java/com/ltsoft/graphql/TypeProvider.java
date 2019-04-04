package com.ltsoft.graphql;

import graphql.language.Definition;
import graphql.schema.idl.RuntimeWiring;

import java.util.function.UnaryOperator;

/**
 * GraphQL Definition 自定义提供者
 *
 * @param <T> Definition 类型
 */
public interface TypeProvider<T extends Definition> {

    /**
     * 获取自定义 Definition
     *
     * @return GraphQL Definition
     */
    Definition<T> getDefinition();

    /**
     * 获取 RuntimeWiring 处理器
     *
     * @return RuntimeWiring.Builder UnaryOperator
     */
    default UnaryOperator<RuntimeWiring.Builder> getWiringOperator() {
        return UnaryOperator.identity();
    }

}
