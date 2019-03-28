package com.ltsoft.graphql;

import com.ltsoft.graphql.example.*;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import graphql.schema.idl.RuntimeWiring;
import org.junit.Test;

import static com.ltsoft.graphql.annotations.GraphQLName.ROOT_MUTATION;
import static com.ltsoft.graphql.annotations.GraphQLName.ROOT_QUERY;
import static com.ltsoft.graphql.example.HelloObject.HelloObjectScalar;
import static org.assertj.core.api.Assertions.assertThat;

public class GraphQLRuntimeWiringBuilderTest {

    @Test
    public void test() {
        RuntimeWiring runtimeWiring = new GraphQLRuntimeWiringBuilder()
                .setInstanceFactory(new DefaultInstanceFactory())
                .withScalar(HelloObjectScalar)
                .withType(EnumObject.class)
                .withType(RootSchemaService.class, RootQueryService.class, RootMutationService.class)
                .withType(NormalInterface.class)
                .withType(MutationObject.class)
                .builder()
                .build();

        assertThat(runtimeWiring.getEnumValuesProviders()).containsOnlyKeys("EnumObject");
        assertThat(runtimeWiring.getScalars()).containsValue(HelloObjectScalar);
        assertThat(runtimeWiring.getDataFetcherForType(ROOT_QUERY)).containsKeys("hello", "fieldDefinitionName", "fieldTypeName", "unknownArgument");
        assertThat(runtimeWiring.getDataFetcherForType(ROOT_MUTATION)).containsKeys("parse");
        assertThat(runtimeWiring.getTypeResolvers()).containsKeys("NormalInterface");
    }

}
