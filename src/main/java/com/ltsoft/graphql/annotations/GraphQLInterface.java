package com.ltsoft.graphql.annotations;

import com.ltsoft.graphql.resolver.DefaultTypeResolver;
import graphql.schema.TypeResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface GraphQLInterface {

    Class<? extends TypeResolver> typeResolver() default DefaultTypeResolver.class;

}
