package com.ltsoft.graphql.example.custom;

import com.ltsoft.graphql.annotations.GraphQLType;

import java.util.List;

@GraphQLType
public class ObjectForTypeResolver {

    private List<CustomType<String>> customType;

    public List<CustomType<String>> getCustomType() {
        return customType;
    }

    public void setCustomType(List<CustomType<String>> customType) {
        this.customType = customType;
    }


}
