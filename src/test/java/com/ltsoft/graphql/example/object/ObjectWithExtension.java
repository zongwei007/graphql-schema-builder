package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.GraphQLComment;
import com.ltsoft.graphql.annotations.GraphQLFieldExtension;
import com.ltsoft.graphql.annotations.GraphQLType;

@GraphQLType
@GraphQLFieldExtension(ObjectDefineExtension.class)
@GraphQLComment("It is a comment")
public class ObjectWithExtension {
}
