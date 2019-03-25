package com.ltsoft.graphql.impl;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class JavaTypeResolver implements TypeResolver {
    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        Object source = env.getObject();
        String typeName = resolveTypeName(source.getClass());

        return env.getSchema().getObjectType(typeName);
    }
}
