package com.ltsoft.graphql.impl;

import com.google.common.collect.ImmutableMap;
import com.ltsoft.graphql.example.ArgumentService;
import com.ltsoft.graphql.example.MutationObject;
import graphql.schema.DataFetchingEnvironment;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphQLArgumentProviderTest {

    @Test
    public void hello() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("hello", String.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("name")).thenReturn("world");

        Object argument = provider.provide(environment);

        assertThat(argument).isEqualTo("world");
    }

    @Test
    public void helloAsObject() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("helloAsObj", MutationObject.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArguments()).thenReturn(ImmutableMap.of(
                "id", 2L,
                "name", "foo",
                "parent", ImmutableMap.of(
                        "id", 1L,
                        "name", "bar"
                ))
        );

        Object argument = provider.provide(environment);

        assertThat(provider.isGenericType()).isTrue();
        assertThat(argument).isInstanceOf(MutationObject.class)
                .satisfies(object -> {
                    MutationObject mutationObject = (MutationObject) object;

                    assertThat(mutationObject.getId()).isEqualTo(2L);
                    assertThat(mutationObject.getName()).isEqualTo("foo");
                    assertThat(mutationObject.getParent()).isInstanceOf(MutationObject.class);
                    assertThat(mutationObject.getParent().getId()).isEqualTo(1L);
                    assertThat(mutationObject.getParent().getName()).isEqualTo("bar");
                    assertThat(mutationObject.getParent().getParent()).isNull();
                });
    }

    @Test
    public void simpleList() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("simpleList", List.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("list")).thenReturn(Arrays.asList("a", "b", "c"));

        Object argument = provider.provide(environment);

        assertThat(provider.getArgumentName()).isEqualTo("list");
        assertThat(argument).isInstanceOf(List.class);
        //noinspection unchecked
        assertThat((List) argument).containsOnly("a", "b", "c");
    }

    @Test
    public void mapList() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("mapList", List.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("list")).thenReturn(Arrays.asList(
                ImmutableMap.of("k1", 1, "k2", 2),
                ImmutableMap.of("k3", 3, "k4", 4)
        ));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(List.class);
        //noinspection unchecked
        assertThat((List) argument)
                .hasSize(2)
                .contains(ImmutableMap.of("k1", 1, "k2", 2));
    }

    @Test
    public void basicList() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("basicList", List.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("list")).thenReturn(Arrays.asList(
                ImmutableMap.of("id", 1L, "name", "foo"),
                ImmutableMap.of("id", 2L, "name", "bar")
        ));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(List.class);
        //noinspection unchecked
        assertThat((List) argument).hasSize(2)
                .hasOnlyElementsOfType(MutationObject.class);
    }

    @Test
    public void map() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("map", LinkedHashMap.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("input")).thenReturn(ImmutableMap.of("id", 1L, "name", "foo"));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(LinkedHashMap.class);
        //noinspection unchecked
        assertThat((Map) argument).containsKeys("id", "name");
    }

    @Test
    public void basic() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("basic", MutationObject.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("input")).thenReturn(ImmutableMap.of("id", 1L, "name", "foo"));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(MutationObject.class);
        assertThat(((MutationObject) argument).getId()).isEqualTo(1L);
    }

    private GraphQLArgumentProvider buildArgumentProvider(String name, Class<?>... argumentType) throws NoSuchMethodException {
        Method method = ArgumentService.class.getMethod(name, argumentType);
        Parameter parameter = method.getParameters()[0];

        return new GraphQLArgumentProvider(ArgumentService.class, method, parameter, new DefaultInstanceFactory());
    }
}
