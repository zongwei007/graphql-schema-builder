package com.ltsoft.graphql;

import com.ltsoft.graphql.impl.DefaultServiceInstanceFactory;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TypeDefinitionRegistryBuilderTest {

    @Test
    public void build() {
        ServiceInstanceFactory factory = new DefaultServiceInstanceFactory();

        TypeDefinitionRegistry registry = new TypeDefinitionRegistryBuilder()
                .setServiceInstanceFactory(factory)
                .register("com.ltsoft.graphql.example")
                .build();

        Assertions.assertThat(registry).isNotNull();
    }
}
