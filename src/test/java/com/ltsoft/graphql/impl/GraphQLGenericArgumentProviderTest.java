package com.ltsoft.graphql.impl;

import com.google.common.collect.ImmutableMap;
import com.ltsoft.graphql.example.input.MutationInputObject;
import com.ltsoft.graphql.example.object.GenericServiceImpl;
import com.ltsoft.graphql.example.object.MutationObject;
import graphql.schema.DataFetchingEnvironment;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphQLGenericArgumentProviderTest {

    @Test
    public void generic() {
        GraphQLArgumentProvider provider = buildArgumentProvider("generic");

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArguments()).thenReturn(ImmutableMap.of("id", 1L, "name", "foo"));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(MutationObject.class);
        assertThat(((MutationObject) argument).getId()).isEqualTo(1L);
    }

    @Test
    public void genericAll() {
        GraphQLArgumentProvider provider = buildArgumentProvider("genericList");
        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("list")).thenReturn(Arrays.asList(
                ImmutableMap.of("id", 1L, "name", "foo"),
                ImmutableMap.of("id", 2L, "name", "bar")
        ));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(List.class);
        //noinspection unchecked
        assertThat((List) argument).hasSize(2)
                .hasOnlyElementsOfType(MutationInputObject.class);
    }

    private GraphQLArgumentProvider buildArgumentProvider(String name) {
        Method method = Arrays.stream(GenericServiceImpl.class.getMethods())
                .filter(ele -> name.equals(ele.getName()))
                .filter(ele -> !ele.isBridge())
                .findFirst()
                .orElse(null);

        if (method == null) {
            throw new IllegalArgumentException();
        }

        Parameter parameter = method.getParameters()[0];

        return new GraphQLArgumentProvider(GenericServiceImpl.class, parameter, Arrays.asList(
                new CollectionArgumentConverter(), new ScalarTypeArgumentConverter(),
                new BeanArgumentConverter(new DefaultInstanceFactory())
        ));
    }

}
