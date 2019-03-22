package com.ltsoft.graphql.visibility;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLType;

import java.util.Objects;
import java.util.function.BiPredicate;

public interface TypeVisibilityFilter extends BiPredicate<GraphQLType, GraphQLFieldDefinition> {

    default TypeVisibilityFilter and(TypeVisibilityFilter other) {
        Objects.requireNonNull(other);

        return (type, fieldDefinition) -> test(type, fieldDefinition) && other.test(type, fieldDefinition);
    }
}
