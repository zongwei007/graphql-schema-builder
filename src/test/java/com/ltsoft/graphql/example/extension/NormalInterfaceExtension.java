package com.ltsoft.graphql.example.extension;

import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.example.iface.NormalInterface;

@GraphQLTypeExtension(NormalInterface.class)
public interface NormalInterfaceExtension {

    String[] items();

}
