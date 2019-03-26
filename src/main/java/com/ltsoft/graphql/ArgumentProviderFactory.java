package com.ltsoft.graphql;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ArgumentProviderFactory<T> {

    boolean isSupport(Class<?> cls, Method method, Parameter parameter);

    ArgumentProvider<T> build(Class<?> cls, Method method, Parameter parameter);
}
