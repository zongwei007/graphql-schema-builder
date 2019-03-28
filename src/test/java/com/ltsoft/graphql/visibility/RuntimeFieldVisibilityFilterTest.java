package com.ltsoft.graphql.visibility;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.schema.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeFieldVisibilityFilterTest {

    @Test
    public void test() {
        RuntimeFieldVisibilityFilter filter = new RuntimeFieldVisibilityFilter(
                ImmutableList.of(new TypeVisibilityTestFilter(), (type, definition) -> true),
                ImmutableList.of(new FieldVisibilityTestFilter(), (type, fieldName) -> true)
        );

        GraphQLFieldsContainer objectType = GraphQLObjectType.newObject()
                .name("Object")
                .field(builder -> builder.name("foo").type(Scalars.GraphQLString))
                .field(builder -> builder.name("bar").type(Scalars.GraphQLBigInteger))
                .build();

        GraphQLInputFieldsContainer inputType = GraphQLInputObjectType.newInputObject()
                .name("Input")
                .field(builder -> builder.name("foo").type(Scalars.GraphQLString))
                .field(builder -> builder.name("bar").type(Scalars.GraphQLBigInteger))
                .build();

        assertThat(filter.getFieldDefinitions(objectType)).hasSize(1);
        assertThat(filter.getFieldDefinition(objectType, "foo")).isNull();
        assertThat(filter.getFieldDefinition(objectType, "bar")).isNotNull();
        assertThat(filter.getFieldDefinition(inputType, "foo")).isNull();
        assertThat(filter.getFieldDefinition(inputType, "bar")).isNotNull();
    }

    private static class TypeVisibilityTestFilter implements TypeVisibilityFilter {
        @Override
        public boolean test(GraphQLType type, GraphQLFieldDefinition definition) {
            return definition.getName().equals("foo");
        }
    }

    private static class FieldVisibilityTestFilter implements FieldVisibilityFilter {

        @Override
        public boolean test(GraphQLType type, String fieldName) {
            return fieldName.equals("bar");
        }
    }
}
