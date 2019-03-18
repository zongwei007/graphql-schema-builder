package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLArgument;
import com.ltsoft.graphql.annotations.GraphQLView;
import com.ltsoft.graphql.view.CreatedView;
import com.ltsoft.graphql.view.UpdatedView;

public class MutationService {

    @GraphQLView(CreatedView.class)
    public MutationObject create(@GraphQLArgument MutationObject item) {
        return null;
    }

    @GraphQLView(UpdatedView.class)
    public MutationObject update(@GraphQLArgument MutationObject item) {
        return null;
    }

    public Integer delete() {
        return 0;
    }

}
