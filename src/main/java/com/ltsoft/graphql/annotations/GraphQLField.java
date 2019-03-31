package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明一个 GraphQL Field。
 * 默认情况下，解析器会尝试将当前类中所有字段识别为 GraphQL Field，
 * 但若有任意 {@link Parameter} 或 {@link Method} 声明了 @GraphQLField，
 * 则仅识别声明了 @GraphQLField 的 {@link Parameter} 或 {@link Method}。
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, METHOD})
@Inherited
public @interface GraphQLField {
}
