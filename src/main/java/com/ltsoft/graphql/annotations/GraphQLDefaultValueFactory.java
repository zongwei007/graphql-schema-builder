package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({PARAMETER, FIELD, METHOD})
@Inherited
public @interface GraphQLDefaultValueFactory {

    Class<? extends Supplier<?>> value();

}
