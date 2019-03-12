package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * GraphQL 类型
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, FIELD})
@Inherited
public @interface GraphQLName {

    String value();

}
