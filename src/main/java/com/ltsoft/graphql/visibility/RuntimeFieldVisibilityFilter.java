package com.ltsoft.graphql.visibility;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputFieldsContainer;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.visibility.GraphqlFieldVisibility;

import java.util.List;
import java.util.stream.Collectors;

public class RuntimeFieldVisibilityFilter implements GraphqlFieldVisibility {

    private final List<TypeVisibilityFilter> typeFilter;

    private final List<FieldVisibilityFilter> fieldFilter;

    public RuntimeFieldVisibilityFilter(List<TypeVisibilityFilter> typeFilter, List<FieldVisibilityFilter> fieldFilter) {
        this.typeFilter = typeFilter;
        this.fieldFilter = fieldFilter;
    }

    private TypeVisibilityFilter checkType() {
        return typeFilter.stream()
                .reduce(TypeVisibilityFilter::and)
                .orElse((type, definition) -> true);
    }

    private FieldVisibilityFilter checkField() {
        return fieldFilter.stream()
                .reduce(FieldVisibilityFilter::and)
                .orElse((type, fieldName) -> true);
    }

    @Override
    public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
        TypeVisibilityFilter typePredicate = checkType();

        return fieldsContainer.getFieldDefinitions().stream()
                .filter(ele -> typePredicate.test(fieldsContainer, ele))
                .collect(Collectors.toList());
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
        if (checkField().test(fieldsContainer, fieldName)) {
            return fieldsContainer.getFieldDefinition(fieldName);
        }

        return null;
    }

    @Override
    public GraphQLInputObjectField getFieldDefinition(GraphQLInputFieldsContainer fieldsContainer, String fieldName) {
        if (checkField().test(fieldsContainer, fieldName)) {
            return fieldsContainer.getFieldDefinition(fieldName);
        }

        return null;
    }
}
