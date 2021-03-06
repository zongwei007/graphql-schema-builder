package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * GraphQL 输入类型
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface GraphQLInput {
}
