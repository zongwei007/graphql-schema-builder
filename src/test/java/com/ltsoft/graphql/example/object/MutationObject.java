package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.example.input.MutationInputObject;
import com.ltsoft.graphql.impl.EmptyDataFetcherFactory;
import com.ltsoft.graphql.view.CreatedView;

@GraphQLType
@GraphQLDefaultDataFetcher(EmptyDataFetcherFactory.class)
public class MutationObject extends AbstractNameObject {

    @GraphQLField
    @GraphQLIgnore(view = CreatedView.class)
    @GraphQLNotNull
    private Long id;

    @GraphQLField
    @GraphQLMutationType(MutationInputObject.class)
    private MutationObject parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MutationObject getParent() {
        return parent;
    }

    public void setParent(MutationObject parent) {
        this.parent = parent;
    }
}
