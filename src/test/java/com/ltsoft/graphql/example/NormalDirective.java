package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLDirectiveLocations;
import com.ltsoft.graphql.annotations.GraphQLNotNull;

import static graphql.introspection.Introspection.DirectiveLocation.*;

@GraphQLDirectiveLocations({INTERFACE, FIELD_DEFINITION, ARGUMENT_DEFINITION})
public interface NormalDirective {

    Integer page();

    @GraphQLNotNull
    String name();

}
