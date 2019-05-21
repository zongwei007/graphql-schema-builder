package com.ltsoft.graphql;

import com.google.common.reflect.TypeToken;

import java.util.function.BiFunction;

/**
 * ServiceDataFetcher 参数转换器
 *
 * @param <T> 输出参数类型
 */
@SuppressWarnings("UnstableApiUsage")
public interface ArgumentConverter<T> {

    /**
     * 测试转换器是否支持某种输出类型
     *
     * @param from 输入类型
     * @param to   输出类型
     * @return 是否支持指定类型
     */
    Boolean isSupport(TypeToken<?> from, TypeToken<?> to);

    /**
     * 将输入数据转换为指定类型
     *
     * @param source 输入数据
     * @param type   输出类型
     * @param orElse 其它类型的转换支持
     * @return 输出类型
     */
    T convert(Object source, TypeToken<T> type, BiFunction<Object, TypeToken<?>, ?> orElse);
}
