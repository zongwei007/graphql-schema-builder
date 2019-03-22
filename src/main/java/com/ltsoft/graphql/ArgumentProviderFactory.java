package com.ltsoft.graphql;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;

public interface ArgumentProviderFactory<T> extends BiFunction<Parameter, Method, ArgumentProvider<T>> {

    boolean isSupport(Parameter parameter, Method method);

}
