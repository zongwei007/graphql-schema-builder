package com.ltsoft.graphql;

import com.ltsoft.graphql.impl.ServiceDataFetcher;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * ServiceDataFetcher 方法参数提供者工厂
 *
 * @param <T> 参数类型
 * @see ServiceDataFetcher
 */
public interface ArgumentProviderFactory<T> {

    /**
     * 测试提供者是否支持某个方法参数
     *
     * @param cls       方法所属的类型
     * @param method    方法
     * @param parameter 参数类型
     * @return 是否支持该参数
     */
    boolean isSupport(Class<?> cls, Method method, Parameter parameter);

    /**
     * 构造参数提供者
     *
     * @param cls       方法所属的类型
     * @param method    方法
     * @param parameter 参数类型
     * @return 参数提供者
     */
    ArgumentProvider<T> build(Class<?> cls, Method method, Parameter parameter);
}
