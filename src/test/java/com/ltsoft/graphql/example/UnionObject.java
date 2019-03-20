package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLUnion;

@GraphQLUnion(possibleTypes = {NormalObject.class, NormalInterfaceImpl.class})
public interface UnionObject {
}
