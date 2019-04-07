package com.ltsoft.graphql;

import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.NamedNode;
import graphql.schema.idl.RuntimeWiring;

import java.util.function.UnaryOperator;

/**
 * GraphQL Definition 提供者
 *
 * @param <T> Definition 类型
 */
public interface TypeProvider<T extends Definition> {

    /**
     * 获取 GraphQL Definition
     *
     * @return Definition
     */
    Definition<T> getDefinition();

    default String getTypeName() {
        Definition<T> definition = getDefinition();

        if (definition instanceof NamedNode) {
            return ((NamedNode) definition).getName();
        }

        throw new IllegalArgumentException(String.format("Can not find type name case '%s' is not a NamedNode", definition));
    }

    /**
     * 获取 Document 处理器
     *
     * @return Document.Builder UnaryOperator
     */
    default UnaryOperator<Document.Builder> getDefinitionOperator() {
        return builder -> builder.definition(getDefinition());
    }

    /**
     * 获取 RuntimeWiring 处理器
     *
     * @return RuntimeWiring.Builder UnaryOperator
     */
    default UnaryOperator<RuntimeWiring.Builder> getWiringOperator() {
        return UnaryOperator.identity();
    }

}
