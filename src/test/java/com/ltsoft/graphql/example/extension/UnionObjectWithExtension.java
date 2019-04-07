package com.ltsoft.graphql.example.extension;

import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.annotations.GraphQLUnion;
import com.ltsoft.graphql.example.object.MutationObject;
import com.ltsoft.graphql.example.union.UnionObject;

@GraphQLTypeExtension(UnionObject.class)
@GraphQLUnion(possibleTypes = MutationObject.class)
public interface UnionObjectWithExtension {
}
