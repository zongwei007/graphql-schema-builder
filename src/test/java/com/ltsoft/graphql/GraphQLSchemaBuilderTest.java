package com.ltsoft.graphql;

import com.ltsoft.graphql.example.HelloObject;
import com.ltsoft.graphql.example.RootMutationService;
import com.ltsoft.graphql.example.RootQueryService;
import com.ltsoft.graphql.example.RootSchemaService;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQLSchemaBuilderTest {

    @Test
    public void testWithType() {
        InstanceFactory instanceFactory = new DefaultInstanceFactory();

        GraphQLSchema schema = new GraphQLSchemaBuilder()
                .addScalar(HelloObject.HelloObjectScalar, HelloObject.class)
                .instanceFactory(instanceFactory)
                .withType(RootSchemaService.class, RootQueryService.class, RootMutationService.class)
                .build();

        assertThat(schema.getObjectType("Query")).isNotNull();
        assertThat(schema.getObjectType("Mutation")).isNotNull();
        assertThat(schema.getType("Hello")).isInstanceOf(GraphQLScalarType.class);
        assertThat(schema.getType("DateTime")).isInstanceOf(GraphQLScalarType.class);
    }

    @Test
    public void testWithPackage() {
        InstanceFactory instanceFactory = new DefaultInstanceFactory();

        GraphQLSchema schema = new GraphQLSchemaBuilder()
                .addScalar(HelloObject.HelloObjectScalar, HelloObject.class)
                .instanceFactory(instanceFactory)
                .withPackage("com.ltsoft.graphql.example")
                .build();

        assertThat(schema.getObjectType("Query")).isNotNull();
        assertThat(schema.getObjectType("Mutation")).isNotNull();
    }

}
