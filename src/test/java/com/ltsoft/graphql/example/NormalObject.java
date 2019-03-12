package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.*;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@GraphQLName("Normal")
@GraphQLDescription("Normal GraphQL Object")
public class NormalObject {

    @GraphQLDescription("GraphQL foo field")
    private String foo;

    @GraphQLName("barList")
    private String[] bar;

    @NotNull
    private List<String> fooList;

    @NotNull
    @GraphQLDescription("GraphQL count field")
    public Integer getCount(
            @GraphQLArgument("cnd") @GraphQLDescription("A cnd argument") @GraphQLDefaultValue("1") @NotNull String cnd
    ) {
        return Integer.parseInt(cnd);
    }

    @NotNull
    public Set<OffsetDateTime> filterDateTimes(
            @GraphQLArgument("args") @GraphQLDefaultValueFactory(OffsetDateTimeDefaultValue.class) @NotNull OffsetDateTime... args
    ) {
        return new HashSet<>(Arrays.asList(args));
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public String[] getBar() {
        return bar;
    }

    public void setBar(String[] bar) {
        this.bar = bar;
    }

    public List<String> getFooList() {
        return fooList;
    }

    public void setFooList(List<String> fooList) {
        this.fooList = fooList;
    }

    public static class OffsetDateTimeDefaultValue implements Supplier<OffsetDateTime[]> {
        @Override
        public OffsetDateTime[] get() {
            return new OffsetDateTime[0];
        }
    }
}
