package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.GraphQLDeprecate;
import com.ltsoft.graphql.annotations.GraphQLType;

@GraphQLType
public abstract class ObjectDefineExtension {

    @GraphQLDeprecate("it is deprecate")
    private String doSomething;

    public String getDoSomething() {
        return doSomething;
    }

    public void setDoSomething(String doSomething) {
        this.doSomething = doSomething;
    }
}
