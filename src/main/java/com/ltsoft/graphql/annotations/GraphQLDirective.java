package com.ltsoft.graphql.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Repeatable(GraphQLDirectives.class)
public @interface GraphQLDirective {

    Class<?> type();

    String[] arguments() default {};

}

