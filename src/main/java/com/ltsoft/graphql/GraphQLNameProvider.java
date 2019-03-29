package com.ltsoft.graphql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface GraphQLNameProvider {

    String provide(Class<?> resolvingCls, Method method, Field field);

}
