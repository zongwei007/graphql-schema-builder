package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明一个 GraphQLSupperClass，用于共享 field 信息，但不作为 GraphQL Type 进行解析。
 * 可用于 {@link GraphQLType} 和 {@link GraphQLInput}。
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface GraphQLSupperClass {
}
