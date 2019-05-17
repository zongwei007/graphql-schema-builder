package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.GraphQLArgument;
import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.annotations.GraphQLTypeReference;
import com.ltsoft.graphql.example.input.MutationInputObject;

import java.time.Year;
import java.util.*;

@GraphQLType
public class ArgumentService {

    public String hello(@GraphQLArgument("name") String name) {
        return name;
    }

    public Year scalar(@GraphQLArgument("year") Year year) {
        return year;
    }

    public MutationObject helloAsObj(@GraphQLArgument MutationObject basic) {
        return basic;
    }

    public List<String> simpleList(@GraphQLArgument("list") List<String> list) {
        return list;
    }

    public Integer[] simpleArray(@GraphQLArgument("array") Integer[] array) {
        return array;
    }

    public Set<String> simpleSet(@GraphQLArgument("set") Set<String> set) {
        return set;
    }

    public List<MutationObject> basicList(@GraphQLArgument("list") @GraphQLTypeReference(type = MutationInputObject.class) ArrayList<MutationObject> list) {
        return list;
    }

    public Map<String, String> map(@GraphQLArgument("input") HashMap<String, String> input) {
        return input;
    }

    public MutationObject basic(@GraphQLArgument("input") @GraphQLTypeReference(type = MutationInputObject.class) MutationObject input) {
        return input;
    }

    public List<Map<String, String>> mapList(@GraphQLArgument("list") List<Map<String, String>> list) {
        return list;
    }
}
