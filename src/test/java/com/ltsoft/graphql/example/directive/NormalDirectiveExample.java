package com.ltsoft.graphql.example.directive;

import com.ltsoft.graphql.annotations.GraphQLInterface;

@GraphQLInterface
@NormalDirective(page = 1, name = "foo")
public interface NormalDirectiveExample {

    @NormalDirective(page = 2, name = "bar")
    String info();

}
