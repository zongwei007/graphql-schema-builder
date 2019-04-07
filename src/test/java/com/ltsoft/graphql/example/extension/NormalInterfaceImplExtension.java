package com.ltsoft.graphql.example.extension;

import com.ltsoft.graphql.annotations.GraphQLDescription;
import com.ltsoft.graphql.annotations.GraphQLTypeExtension;

@GraphQLTypeExtension(NormalInterfaceImpl.class)
public class NormalInterfaceImplExtension {

    @GraphQLDescription("A foobar field")
    private String[] items;

    public String[] getItems() {
        return items;
    }

    public void setItems(String[] items) {
        this.items = items;
    }
}
