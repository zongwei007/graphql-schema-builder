package com.ltsoft.graphql.example.enumeration;

import com.ltsoft.graphql.annotations.GraphQLDescription;
import com.ltsoft.graphql.annotations.GraphQLType;

@GraphQLType
public enum EnumObject {

    @GraphQLDescription("GraphQL Enum first")
    first,

    second

}
