package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLArgument;
import com.ltsoft.graphql.annotations.GraphQLNameFormatter;
import com.ltsoft.graphql.annotations.GraphQLType;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@GraphQLType
@GraphQLNameFormatter(GenericService.NameFormatter.class)
public abstract class GenericService<E, F extends E> {

    public E generic(@GraphQLArgument E input) {
        return input;
    }

    public List<E> genericList(@GraphQLArgument("list") List<F> list) {
        return list.stream().map(ele -> (E) ele).collect(Collectors.toList());
    }

    public static class NameFormatter implements BiFunction<String, Class<?>, String> {

        @Override
        public String apply(String name, Class<?> javaType) {
            return name.equals("genericList") ? "genericInputList" : null;
        }
    }
}
