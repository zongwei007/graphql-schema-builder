package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLDirective;
import com.ltsoft.graphql.annotations.GraphQLDirectiveLocations;
import com.ltsoft.graphql.annotations.GraphQLNotNull;
import graphql.introspection.Introspection.DirectiveLocation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Inherited
@GraphQLDirective
@GraphQLDirectiveLocations({DirectiveLocation.OBJECT, DirectiveLocation.FIELD_DEFINITION, DirectiveLocation.INTERFACE})
public @interface NormalDirective {

    int page();

    @GraphQLNotNull
    String name();

}
