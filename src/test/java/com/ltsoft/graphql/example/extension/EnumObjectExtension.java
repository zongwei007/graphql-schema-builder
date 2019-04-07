package com.ltsoft.graphql.example.extension;

import com.ltsoft.graphql.annotations.GraphQLName;
import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.example.enumeration.EnumObject;

@GraphQLTypeExtension(EnumObject.class)
public enum EnumObjectExtension {

    @GraphQLName("no3")
    third

}
