package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLName;
import com.ltsoft.graphql.annotations.GraphQLTypeExtension;

@GraphQLTypeExtension(EnumObject.class)
public enum EnumObjectExtension {

    @GraphQLName("no3")
    third

}
