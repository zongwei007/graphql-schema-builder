package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.view.CreatedView;
import com.ltsoft.graphql.view.UpdatedView;

import java.util.List;

@GraphQLType
public class MutationService {

    @GraphQLField
    @GraphQLView(CreatedView.class)
    public MutationObject create(@GraphQLArgument MutationObject item) {
        return null;
    }

    @GraphQLField
    @GraphQLView(UpdatedView.class)
    public MutationObject update(@GraphQLArgument MutationObject item) {
        return null;
    }

    @GraphQLField
    @GraphQLDataFetcher
    public Integer batch(@GraphQLArgument("items") @GraphQLNotNull List<MutationInputObject> items) {
        return 0;
    }

    public Integer delete() {
        return 0;
    }

}
