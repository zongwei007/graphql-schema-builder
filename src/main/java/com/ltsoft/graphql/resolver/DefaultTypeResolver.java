package com.ltsoft.graphql.resolver;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;

public class DefaultTypeResolver implements TypeResolver {
    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        Object source = env.getObject();
        String typeName = ResolveUtil.resolveTypeName(source.getClass());

        return env.getSchema().getObjectType(typeName);
    }
}
