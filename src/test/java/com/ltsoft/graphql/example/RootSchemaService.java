package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLName;
import com.ltsoft.graphql.annotations.GraphQLType;

import static com.ltsoft.graphql.annotations.GraphQLName.ROOT_SCHEMA;

@GraphQLName(ROOT_SCHEMA)
@GraphQLType
public class RootSchemaService {

    private RootQueryService query = new RootQueryService();

    private RootMutationService mutation = new RootMutationService();

    public RootQueryService getQuery() {
        return query;
    }

    public void setQuery(RootQueryService query) {
        this.query = query;
    }

    public RootMutationService getMutation() {
        return mutation;
    }

    public void setMutation(RootMutationService mutation) {
        this.mutation = mutation;
    }
}
