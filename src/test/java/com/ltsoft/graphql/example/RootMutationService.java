package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.*;

import java.time.OffsetDateTime;

import static com.ltsoft.graphql.annotations.GraphQLName.ROOT_MUTATION;

@GraphQLName(ROOT_MUTATION)
@GraphQLType
public class RootMutationService {

    @GraphQLDataFetcher
    public OffsetDateTime parse(@GraphQLArgument("source") @GraphQLNotNull String source) {
        return OffsetDateTime.parse(source);
    }

}
