package com.ltsoft.graphql.example.iface;

import com.ltsoft.graphql.annotations.GraphQLInterface;

@GraphQLInterface
public abstract class AbstractInterface implements NormalInterface {

    private String ifaceName;

    public String getIfaceName() {
        return ifaceName;
    }

    public void setIfaceName(String ifaceName) {
        this.ifaceName = ifaceName;
    }
}
