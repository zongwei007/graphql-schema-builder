package com.ltsoft.graphql.example.union;

import com.ltsoft.graphql.annotations.GraphQLUnion;
import com.ltsoft.graphql.example.extension.NormalInterfaceImpl;
import com.ltsoft.graphql.example.object.NormalObject;

@GraphQLUnion(possibleTypes = {NormalObject.class, NormalInterfaceImpl.class})
public interface UnionObject {
}
