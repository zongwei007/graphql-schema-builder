package com.ltsoft.graphql;

import com.ltsoft.graphql.example.RootSchemaService;
import com.ltsoft.graphql.example.custom.CustomTypeResolver;
import com.ltsoft.graphql.example.scalar.HelloObject;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import com.ltsoft.graphql.provider.EnumTypeProvider;
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
                .addType(RootSchemaService.class)
                .build();

        assertThat(schema.getObjectType("Query")).isNotNull();
        assertThat(schema.getObjectType("Mutation")).isNotNull();
        assertThat(schema.getType("Hello")).isInstanceOf(GraphQLScalarType.class);
        assertThat(schema.getType("DateTime")).isInstanceOf(GraphQLScalarType.class);
    }

    @Test
    public void testWithPackage() {
        InstanceFactory instanceFactory = new DefaultInstanceFactory();
        TypeProvider<EnumTypeDefinition> customEnumProvider = EnumTypeProvider.newEnumTypeProvider()
                .name("CustomEnum")
                .description("A Custom Enum")
                .enumValues("customFirst", 1)
                .enumValues("customSecond", 2)
                .build();

        GraphQLSchema schema = new GraphQLSchemaBuilder()
                .addScalar(HelloObject.HelloObjectScalar, HelloObject.class)
                .instanceFactory(instanceFactory)
                .addPackage("com.ltsoft.graphql.example")
                .typeProvider(customEnumProvider)
                .typeResolver(new CustomTypeResolver())
                .build();

        assertThat(schema.getObjectType("Query")).isNotNull();
        assertThat(schema.getObjectType("Mutation")).isNotNull();
    }

}
