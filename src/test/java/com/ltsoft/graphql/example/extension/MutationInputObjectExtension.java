package com.ltsoft.graphql.example.extension;

import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.example.input.MutationInputObject;

import java.time.OffsetDateTime;

@GraphQLTypeExtension(MutationInputObject.class)
public class MutationInputObjectExtension {

    private OffsetDateTime now;

    public void setNow(OffsetDateTime now) {
        this.now = now;
    }

}
