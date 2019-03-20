package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLDescription;
import com.ltsoft.graphql.annotations.GraphQLTypeExtension;

@GraphQLTypeExtension(NormalObject.class)
public class NormalObjectExtension {

    @GraphQLDescription("A foobar field")
    private String foobar;

    public String getFoobar() {
        return foobar;
    }

    public void setFoobar(String foobar) {
        this.foobar = foobar;
    }
}
