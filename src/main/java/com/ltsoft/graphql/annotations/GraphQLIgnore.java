package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 忽略 GraphQL 解析
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD})
@Inherited
public @interface GraphQLIgnore {
}
