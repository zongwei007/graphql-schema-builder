package com.ltsoft.graphql.example;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.annotations.GraphQLArgument;
import com.ltsoft.graphql.annotations.GraphQLFieldName;
import com.ltsoft.graphql.annotations.GraphQLType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@GraphQLType
@GraphQLFieldName(GenericService.NameFormatter.class)
public abstract class GenericService<E, F extends E> {

    public E generic(@GraphQLArgument E input) {
        return input;
    }

    public List<E> genericList(@GraphQLArgument("list") List<F> list) {
        return list.stream().map(ele -> (E) ele).collect(Collectors.toList());
    }

    public static class NameFormatter implements BiFunction<String, Class<?>, String> {

        @Override
        @SuppressWarnings("UnstableApiUsage")
        public String apply(String name, Class<?> javaType) {
            Type param = ((ParameterizedType) javaType.getGenericSuperclass()).getActualTypeArguments()[0];
            TypeToken<?> paramType = TypeToken.of(javaType).resolveType(param);

            return name.equals("genericList") ? "generic" + paramType.getRawType().getSimpleName() + "List" : null;
        }
    }
}
