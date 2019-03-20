package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLDeprecate;

public interface ObjectDefineExtension {

    @GraphQLDeprecate("it is deprecate")
    String doSomething();

}
