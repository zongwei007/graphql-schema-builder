package com.ltsoft.graphql.impl;

import com.google.common.collect.ImmutableMap;
import com.ltsoft.graphql.ArgumentConverter;
import com.ltsoft.graphql.example.object.ArgumentService;
import com.ltsoft.graphql.example.object.MutationObject;
import graphql.schema.DataFetchingEnvironment;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Year;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    public void nullable() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("helloAsObj", MutationObject.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArguments()).thenReturn(new HashMap<String, Object>() {{
            put("id", 2L);
            put("name", "foo");
            put("parent", null);
        }});

        Object argument = provider.provide(environment);

        assertThat(provider.isGenericType()).isTrue();
        assertThat(argument).isInstanceOf(MutationObject.class)
                .satisfies(object -> {
                    MutationObject mutationObject = (MutationObject) object;

                    assertThat(mutationObject.getId()).isEqualTo(2L);
                    assertThat(mutationObject.getName()).isEqualTo("foo");
                    assertThat(mutationObject.getParent()).isNull();
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
        GraphQLArgumentProvider provider = buildArgumentProvider("basicList", ArrayList.class);

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
        GraphQLArgumentProvider provider = buildArgumentProvider("map", HashMap.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("input")).thenReturn(ImmutableMap.of("id", 1L, "name", "foo"));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(Map.class);
        //noinspection unchecked
        assertThat((Map) argument).containsKeys("id", "name");
    }

    @Test
    public void basic() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("basic", MutationObject.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("input")).thenReturn(ImmutableMap.of(
                "id", 1L, "name", "foo", "something", "nothing"
        ));

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(MutationObject.class);
        assertThat(((MutationObject) argument).getId()).isEqualTo(1L);
    }

    @Test
    public void scalar() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("scalar", Year.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("year")).thenReturn(2019);

        Object argument = provider.provide(environment);

        assertThat(argument).isInstanceOf(Year.class);
        assertThat(((Year) argument)).isEqualTo(Year.of(2019));
    }

    @Test
    public void simpleArray() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("simpleArray", Integer[].class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("array")).thenReturn(Arrays.asList(1, 2, 3, 4));

        Object argument = provider.provide(environment);

        assertThat(provider.getArgumentName()).isEqualTo("array");
        assertThat(argument).isInstanceOf(Integer[].class);
        assertThat((Integer[]) argument).containsOnly(1, 2, 3, 4);
    }

    @Test
    public void simpleSet() throws Exception {
        GraphQLArgumentProvider provider = buildArgumentProvider("simpleSet", Set.class);

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArgument("set")).thenReturn(Arrays.asList("a", "a", "b"));

        Object argument = provider.provide(environment);

        assertThat(provider.getArgumentName()).isEqualTo("set");
        assertThat(argument).isInstanceOf(Set.class);
        //noinspection unchecked
        assertThat((Set) argument).containsOnly("a", "b");
    }

    @Test
    public void converter() throws Exception {
        Method method = ArgumentService.class.getMethod("helloAsObj", MutationObject.class);
        Parameter parameter = method.getParameters()[0];

        Object result = new MutationObject();

        //noinspection unchecked
        ArgumentConverter<Object> converter = mock(ArgumentConverter.class);
        when(converter.isSupport(eq(MutationObject.class))).thenReturn(true);
        when(converter.convert(Mockito.anyMap(), eq(MutationObject.class))).thenReturn(result);

        GraphQLArgumentProvider provider = new GraphQLArgumentProvider(
                ArgumentService.class, method, parameter, new DefaultInstanceFactory(), Collections.singletonList(converter));

        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        when(environment.getArguments()).thenReturn(ImmutableMap.of());

        Object argument = provider.provide(environment);

        assertThat(provider.isGenericType()).isTrue();
        assertThat(argument).isInstanceOf(MutationObject.class).isEqualTo(result);

        verify(converter, atLeastOnce()).isSupport(any());
        verify(converter).convert(anyMap(), any());
    }

    private GraphQLArgumentProvider buildArgumentProvider(String name, Class<?>... argumentType) throws NoSuchMethodException {
        Method method = ArgumentService.class.getMethod(name, argumentType);
        Parameter parameter = method.getParameters()[0];

        return new GraphQLArgumentProvider(ArgumentService.class, method, parameter, new DefaultInstanceFactory(), Collections.emptyList());
    }
}
