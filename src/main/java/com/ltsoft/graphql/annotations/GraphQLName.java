package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * GraphQL 类型
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD})
public @interface GraphQLName {

    String ROOT_SCHEMA = "schema";

    String ROOT_QUERY = "Query";

    String ROOT_MUTATION = "Mutation";

    String value();

}
