package com.ltsoft.graphql;

import com.ltsoft.graphql.example.EnumObject;
import graphql.schema.idl.RuntimeWiring;
import org.junit.Test;

import static com.ltsoft.graphql.example.HelloObject.HelloObjectScalar;
import static org.assertj.core.api.Assertions.assertThat;

public class GraphQLRuntimeWiringBuilderTest {

    @Test
    public void test() {
        RuntimeWiring runtimeWiring = new GraphQLRuntimeWiringBuilder()
                .withScalar(HelloObjectScalar)
                .withType(EnumObject.class)
                .builder()
                .build();

        assertThat(runtimeWiring.getEnumValuesProviders()).containsOnlyKeys("EnumObject");
        assertThat(runtimeWiring.getScalars()).containsValue(HelloObjectScalar);
    }

}
