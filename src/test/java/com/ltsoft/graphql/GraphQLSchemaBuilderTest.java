package com.ltsoft.graphql;

import com.ltsoft.graphql.example.*;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import com.ltsoft.graphql.resolver.ResolveTestUtil;
import graphql.language.EnumTypeDefinition;
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
                .addType(RootSchemaService.class, RootQueryService.class, RootMutationService.class)
                .build();

        assertThat(schema.getObjectType("Query")).isNotNull();
        assertThat(schema.getObjectType("Mutation")).isNotNull();
        assertThat(schema.getType("Hello")).isInstanceOf(GraphQLScalarType.class);
        assertThat(schema.getType("DateTime")).isInstanceOf(GraphQLScalarType.class);
    }

    @Test
    public void testWithPackage() {
        InstanceFactory instanceFactory = new DefaultInstanceFactory();
        EnumTypeDefinition customEnum = ResolveTestUtil.buildCustomEnumDefinition();

        GraphQLSchema schema = new GraphQLSchemaBuilder()
                .addScalar(HelloObject.HelloObjectScalar, HelloObject.class)
                .instanceFactory(instanceFactory)
                .addPackage("com.ltsoft.graphql.example")
                .typeResolver(new ObjectForTypeResolver.CustomTypeResolver())
                .document(document -> document.definition(customEnum))
                .build();

        assertThat(schema.getObjectType("Query")).isNotNull();
        assertThat(schema.getObjectType("Mutation")).isNotNull();
    }

}
