package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@GraphQLType
@GraphQLName("Normal")
@GraphQLDescription("Normal GraphQL Object")
public class NormalObject {

    @GraphQLDescription("GraphQL foo field")
    private String foo;

    @GraphQLName("barList")
    private String[] bar;

    @GraphQLNotNull
    private List<String> fooList;

    @GraphQLTypeReference(name = "CustomEnum")
    private Integer type;

    @GraphQLNotNull
    @GraphQLDescription("GraphQL count field")
    public Integer getCount(@GraphQLArgument("cnd") @GraphQLDescription("A cnd argument") @GraphQLDefaultValue("1") @GraphQLNotNull String cnd) {
        return Integer.parseInt(cnd);
    }

    @GraphQLNotNull
    public Set<OffsetDateTime> filterDateTimes(@GraphQLArgument("args") @GraphQLNotNull OffsetDateTime... args) {
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public static class OffsetDateTimeDefaultValue implements Supplier<OffsetDateTime[]> {
        @Override
        public OffsetDateTime[] get() {
            return new OffsetDateTime[0];
        }
    }
}
