package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLDirective;
import com.ltsoft.graphql.annotations.GraphQLInterface;

@GraphQLInterface
@GraphQLDirective(type = NormalDirective.class, arguments = {"page: 1", "name: foo"})
public interface NormalDirectiveExample {

    @GraphQLDirective(type = NormalDirective.class, arguments = {"page: 2", "name: bar"})
    String info();

}
