package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.*;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import static com.ltsoft.graphql.annotations.GraphQLName.ROOT_QUERY;

@GraphQLName(ROOT_QUERY)
@GraphQLType
public class RootQueryService {

    @GraphQLNotNull
    @GraphQLDataFetcher
    public HelloObject hello(@GraphQLArgument("name") @GraphQLDefaultValue("world!") HelloObject name) {
        return name;
    }

    @GraphQLDataFetcher
    public String getFieldDefinitionName(@GraphQLEnvironment("fieldDefinition") GraphQLFieldDefinition fieldDefinition) {
        return fieldDefinition.getName();
    }

    @GraphQLDataFetcher
    public String getFieldTypeName(DataFetchingEnvironment environment) {
        return environment.getFieldType().getName();
    }

    @GraphQLDataFetcher
    public Integer unknownArgument(Integer number) {
        return number != null ? number : 0;
    }
}
