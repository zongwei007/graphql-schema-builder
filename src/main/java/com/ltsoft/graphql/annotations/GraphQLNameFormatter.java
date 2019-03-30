package com.ltsoft.graphql.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface GraphQLNameFormatter {

    Class<? extends BiFunction<String, Class<?>, String>> value();

}
