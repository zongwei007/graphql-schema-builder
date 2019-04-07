package com.ltsoft.graphql;

import graphql.language.Definition;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * GraphQL Definition 解析器
 *
 * @param <T> GraphQL Definition 类型
 */
public interface TypeResolver<T extends Definition> {

    /**
     * 是否支持某种 Java {@link Type}
     *
     * @param javaType Java 类型
     * @return 支持情况
     */
    boolean isSupport(Type javaType);

    /**
     * 将 Java Type 解析为 {@link TypeProvider}
     *
     * @param javaType Java 类型
     * @param resolver 工具函数，TypeResolver 可以从该方法中查询任意 Java 类型对应的 TypeProvider。
     *                 若解析失败则抛出 {@link IllegalArgumentException}。
     * @return TypeProvider
     */
    TypeProvider<T> resolve(Type javaType, Function<Type, TypeProvider<?>> resolver);
}
