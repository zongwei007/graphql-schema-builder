package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GraphQLType
@GraphQLName("Normal")
@GraphQLDescription("Normal GraphQL Object")
public class NormalObject {

    @GraphQLField
    @GraphQLDescription("GraphQL foo field")
    private String foo;

    @GraphQLField
    @GraphQLName("barList")
    private String[] bar;

    @GraphQLField
    @GraphQLNotNull
    private List<String> fooList;

    @GraphQLField
    @GraphQLDeprecate("type is deprecate")
    private Integer type;

    @GraphQLField
    @GraphQLNotNull
    @GraphQLDescription("GraphQL count field")
    public Integer getCount(@GraphQLArgument("cnd") @GraphQLDescription("A cnd argument") @GraphQLDefaultValue("1") @GraphQLNotNull String cnd) {
        return Integer.parseInt(cnd);
    }

    @GraphQLField
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
}
