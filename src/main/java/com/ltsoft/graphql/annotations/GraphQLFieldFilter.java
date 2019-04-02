package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiPredicate;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明 GraphQL Field 过滤器
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
public @interface GraphQLFieldFilter {

    Class<? extends BiPredicate<Method, Field>> value();

}
