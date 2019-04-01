package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明一个 GraphQL Field 参数，一般与 {@link GraphQLDataFetcher} 同时使用。
 * 如果参数类型为 GraphQL Object 时，会将 GraphQL Object 按所有字段拆解为独立参数，即泛参数解析机制。
 * 可以使用 {@link GraphQLTypeReference} 声明 GraphQL 参数类型，但需自行确认声明的 GraphQL 参数类型能被解析为 Java 的参数类型。
 *
 * @see GraphQLDataFetcher
 * @see GraphQLTypeReference
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
public @interface GraphQLArgument {

    /**
     * GraphQL 参数名。不填写则按 {@link Parameter#getName()} 读取。
     *
     * @return 参数名
     */
    String value() default "";

}
