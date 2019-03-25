package com.ltsoft.graphql.annotations;

import com.ltsoft.graphql.impl.JavaTypeResolver;
import graphql.schema.TypeResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface GraphQLInterface {

    Class<? extends TypeResolver> typeResolver() default JavaTypeResolver.class;

}
