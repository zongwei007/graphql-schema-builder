package com.ltsoft.graphql.example;

import com.ltsoft.graphql.GraphQLNameProvider;
import com.ltsoft.graphql.annotations.GraphQLNameFactory;
import com.ltsoft.graphql.annotations.GraphQLType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@GraphQLType
@GraphQLNameFactory(GenericServiceImpl.NameProvider.class)
public class GenericServiceImpl extends GenericService<MutationObject, MutationInputObject> {

    public static class NameProvider implements GraphQLNameProvider {
        @Override
        public String provide(Class<?> resolvingCls, Method method, Field field) {
            return method.getName().equals("genericList") ? "genericInputList" : null;
        }
    }
}
