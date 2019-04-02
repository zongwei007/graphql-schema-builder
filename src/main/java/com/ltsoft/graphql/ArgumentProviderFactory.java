package com.ltsoft.graphql;

import com.ltsoft.graphql.impl.ServiceDataFetcher;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * ServiceDataFetcher 参数解析器工厂
 *
 * @param <T> 参数类型
 * @see ServiceDataFetcher
 */
public interface ArgumentProviderFactory<T> {

    boolean isSupport(Class<?> cls, Method method, Parameter parameter);

    ArgumentProvider<T> build(Class<?> cls, Method method, Parameter parameter);
}
