package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLTypeExtension;

import java.time.OffsetDateTime;

@GraphQLTypeExtension(MutationInputObject.class)
public class MutationInputObjectExtension {

    private OffsetDateTime now;

    public void setNow(OffsetDateTime now) {
        this.now = now;
    }

}
