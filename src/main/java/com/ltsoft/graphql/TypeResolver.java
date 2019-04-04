package com.ltsoft.graphql;

import graphql.language.TypeDefinition;

import java.lang.reflect.Type;

/**
 * GraphQL TypeDefinition 自定义解析器
 *
 * @param <T> GraphQL TypeDefinition 类型
 */
public interface TypeResolver<T extends TypeDefinition> {

    boolean isSupport(Type javaType);

    TypeDefinition<T> resolve(Type javaType);

}
