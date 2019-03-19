package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLUnion;

@GraphQLUnion(possibleTypes = {NormalObject.class, NormalExtendObject.class})
public interface UnionObject {
}
