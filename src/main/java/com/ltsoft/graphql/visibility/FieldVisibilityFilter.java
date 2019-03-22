package com.ltsoft.graphql.visibility;

import graphql.schema.GraphQLType;

import java.util.function.BiPredicate;

public interface FieldVisibilityFilter extends BiPredicate<GraphQLType, String> {

    default FieldVisibilityFilter and(FieldVisibilityFilter other) {
        return (type, fieldName) -> test(type, fieldName) && other.test(type, fieldName);
    }
}
