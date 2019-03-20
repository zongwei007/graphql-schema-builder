package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLFieldExtension;
import com.ltsoft.graphql.annotations.GraphQLType;

@GraphQLType
@GraphQLFieldExtension(ObjectDefineExtension.class)
public class ObjectWithExtension {
}
