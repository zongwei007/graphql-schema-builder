package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.example.NormalObject;
import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavaTypeResolverTest {

    @Test
    public void getType() {
        NormalObject object = new NormalObject();

        TypeResolutionEnvironment env = mock(TypeResolutionEnvironment.class);
        GraphQLSchema schema = mock(GraphQLSchema.class);
        GraphQLObjectType type = mock(GraphQLObjectType.class);
        when(env.getObject()).thenReturn(object);
        when(env.getSchema()).thenReturn(schema);
        when(schema.getObjectType(Mockito.eq("Normal"))).thenReturn(type);

        JavaTypeResolver resolver = new JavaTypeResolver();

        assertThat(resolver.getType(env)).isEqualTo(type);
    }
}
