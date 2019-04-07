package com.ltsoft.graphql.example.iface;

import com.ltsoft.graphql.annotations.GraphQLInterface;

@GraphQLInterface
public interface NormalInterface {

    String getInfo();

    static String doNothing() {
        return null;
    }
}
