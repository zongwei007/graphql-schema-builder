package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.GraphQLArgument;
import com.ltsoft.graphql.annotations.GraphQLDescription;
import com.ltsoft.graphql.annotations.GraphQLType;

@GraphQLType
public class GenericServiceImpl extends GenericService<MutationObject, MutationInputObject> {

    @Override
    @GraphQLDescription("return a MutationObject")
    public MutationObject generic(@GraphQLArgument MutationObject input) {
        return super.generic(input);
    }
}
