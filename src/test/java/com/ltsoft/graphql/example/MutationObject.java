package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLIgnore;
import com.ltsoft.graphql.annotations.GraphQLMutationType;
import com.ltsoft.graphql.annotations.GraphQLNotNull;
import com.ltsoft.graphql.view.CreatedView;

public class MutationObject {

    @GraphQLIgnore(view = CreatedView.class)
    @GraphQLNotNull
    private Long id;

    @GraphQLNotNull(view = CreatedView.class)
    private String name;

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
