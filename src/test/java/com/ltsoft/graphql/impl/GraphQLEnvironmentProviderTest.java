package com.ltsoft.graphql.impl;

import com.google.common.collect.ImmutableMap;
import com.ltsoft.graphql.annotations.GraphQLEnvironment;
import graphql.schema.DataFetchingEnvironment;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class GraphQLEnvironmentProviderTest {

    @Test
    public void apply() {
        GraphQLEnvironment param = Mockito.mock(GraphQLEnvironment.class);
        DataFetchingEnvironment env = Mockito.mock(DataFetchingEnvironment.class);

        Mockito.when(param.value()).thenReturn("arguments");
        Mockito.when(env.getArguments()).thenReturn(ImmutableMap.of("foo", "bar"));

        GraphQLEnvironmentProvider provider = new GraphQLEnvironmentProvider(param);

        //noinspection unchecked
        assertThat(provider.provide(env)).isInstanceOf(Map.class)
                .satisfies(val ->
                        assertThat(((Map<String, String>) val))
                                .containsOnlyKeys("foo")
                                .containsValue("bar")
                );
    }

    @Test
    public void noMethod() {
        GraphQLEnvironment param = Mockito.mock(GraphQLEnvironment.class);

        Mockito.when(param.value()).thenReturn("argument");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new GraphQLEnvironmentProvider(param))
                .withCauseInstanceOf(NoSuchMethodException.class);
    }

}
