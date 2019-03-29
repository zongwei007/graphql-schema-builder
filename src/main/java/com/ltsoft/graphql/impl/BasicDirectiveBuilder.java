package com.ltsoft.graphql.impl;

import com.google.common.collect.BiMap;
import com.ltsoft.graphql.GraphQLDirectiveBuilder;
import graphql.language.*;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveEnumValueMap;

public abstract class BasicDirectiveBuilder<T extends Annotation> implements GraphQLDirectiveBuilder<T> {

    //使用 ThreadLocal 将参数暂存……这个用法有点 Hack 了
    private static final ThreadLocal<List<Argument>> CURRENT_ARGUMENTS = new ThreadLocal<>();

    private <F, R extends Value> void arguments(String name, F[] items, Function<F, R> mapper) {
        List<Value> values = Arrays.stream(items).map(mapper).collect(Collectors.toList());

        List<Argument> arguments = CURRENT_ARGUMENTS.get();

        if (arguments == null) {
            throw new IllegalStateException("Must trigger builder function before access addArgument function");
        }

        if (values.size() > 1) {
            arguments.add(new Argument(name, new ArrayValue(values)));
        } else {
            arguments.add(new Argument(name, values.get(0)));
        }
    }

    @SafeVarargs
    protected final <E> void addArgument(String name, E... items) {
        E first = items[0];

        if (first instanceof Boolean) {
            arguments(name, (Boolean[]) items, BooleanValue::new);
            return;
        }

        if (first instanceof Double || first instanceof Float) {
            arguments(name, items, ele -> new FloatValue(new BigDecimal(String.valueOf(ele))));
            return;
        }

        if (first instanceof Integer || first instanceof Long) {
            arguments(name, items, ele -> new IntValue(new BigInteger(String.valueOf(ele))));
            return;
        }

        if (first instanceof Character || first instanceof String) {
            arguments(name, items, ele -> new StringValue(String.valueOf(ele)));
            return;
        }

        Class<?> itemType = first.getClass();
        if (!itemType.isEnum()) {
            throw new IllegalArgumentException(String.format("Unsupported directive argument type '%s'", itemType.getName()));
        }

        BiMap<Object, String> valueMap = resolveEnumValueMap(itemType).inverse();
        String[] itemNames = Arrays.stream(items).map(valueMap::get).toArray(String[]::new);

        arguments(name, itemNames, EnumValue::new);
    }

    @Override
    public Directive.Builder builder(T annotation) {
        CURRENT_ARGUMENTS.set(new ArrayList<>());

        Directive.Builder builder = apply(annotation, Directive.newDirective());

        if (!CURRENT_ARGUMENTS.get().isEmpty()) {
            builder.arguments(CURRENT_ARGUMENTS.get());
        }

        CURRENT_ARGUMENTS.remove();
        return builder;
    }

    abstract Directive.Builder apply(T annotation, Directive.Builder builder);

}
