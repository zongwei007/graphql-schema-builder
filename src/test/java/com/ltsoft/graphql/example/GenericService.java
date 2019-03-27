package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLArgument;
import com.ltsoft.graphql.annotations.GraphQLType;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLType
public abstract class GenericService<E, F extends E> {

    public E generic(@GraphQLArgument E input) {
        return input;
    }

    public List<E> genericList(@GraphQLArgument("list") List<F> list) {
        return list.stream().map(ele -> (E) ele).collect(Collectors.toList());
    }

}
