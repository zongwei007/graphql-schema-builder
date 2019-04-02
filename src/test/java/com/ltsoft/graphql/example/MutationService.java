package com.ltsoft.graphql.example;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.view.CreatedView;
import com.ltsoft.graphql.view.UpdatedView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiPredicate;

@GraphQLType
@GraphQLFieldFilter(MutationService.FieldFilter.class)
public class MutationService {

    @GraphQLView(CreatedView.class)
    public MutationObject create(@GraphQLArgument MutationObject item) {
        return null;
    }

    @GraphQLView(UpdatedView.class)
    public MutationObject update(@GraphQLArgument MutationObject item) {
        return null;
    }

    @GraphQLDataFetcher
    public Integer batch(@GraphQLArgument("items") @GraphQLTypeReference(type = MutationInputObject.class) @GraphQLNotNull List<MutationObject> items) {
        return 0;
    }

    public Integer delete() {
        return 0;
    }

    public static class FieldFilter implements BiPredicate<Method, Field> {
        @Override
        public boolean test(Method method, Field field) {
            return !method.getName().equals("delete");
        }
    }
}
