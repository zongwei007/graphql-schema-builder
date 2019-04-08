package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.GraphQLField;
import com.ltsoft.graphql.annotations.GraphQLNotNull;
import com.ltsoft.graphql.annotations.GraphQLSupperClass;
import com.ltsoft.graphql.view.CreatedView;

@GraphQLSupperClass
public abstract class AbstractNameObject {

    @GraphQLField
    @GraphQLNotNull(view = CreatedView.class)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
