package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLTypeExtension;

@GraphQLTypeExtension(NormalInterface.class)
public interface NormalInterfaceExtension {

    String[] items();

}
