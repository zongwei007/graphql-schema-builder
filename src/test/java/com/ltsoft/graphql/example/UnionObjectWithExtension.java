package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.annotations.GraphQLUnion;

@GraphQLTypeExtension(UnionObject.class)
@GraphQLUnion(possibleTypes = MutationObject.class)
public interface UnionObjectWithExtension {
}
