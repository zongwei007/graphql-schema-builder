package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.example.input.MutationInputObject;
import com.ltsoft.graphql.impl.EmptyDataFetcherFactory;
import com.ltsoft.graphql.view.CreatedView;

@GraphQLType
@GraphQLDefaultDataFetcher(EmptyDataFetcherFactory.class)
public class MutationObject {

    @GraphQLField
    @GraphQLIgnore(view = CreatedView.class)
    @GraphQLNotNull
    private Long id;

    @GraphQLField
    @GraphQLNotNull(view = CreatedView.class)
    private String name;

    @GraphQLField
    @GraphQLMutationType(MutationInputObject.class)
    private MutationObject parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MutationObject getParent() {
        return parent;
    }

    public void setParent(MutationObject parent) {
        this.parent = parent;
    }
}
