package com.ltsoft.graphql.example.object;

import com.ltsoft.graphql.annotations.GraphQLArgument;
import com.ltsoft.graphql.annotations.GraphQLDescription;
import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.example.input.MutationInputObject;

@GraphQLType
public class GenericServiceImpl extends GenericService<MutationObject, MutationInputObject> {

    @Override
    @GraphQLDescription("return a MutationObject")
    public MutationObject generic(@GraphQLArgument MutationObject input) {
        return super.generic(input);
    }
}
