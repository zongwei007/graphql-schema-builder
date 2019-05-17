package com.ltsoft.graphql;

import java.util.Map;

/**
 * ServiceDataFetcher 参数转换器
 *
 * @param <T> 输出参数类型
 */
public interface ArgumentConverter<T> {

    /**
     * 测试转换器是否支持某种输出类型
     *
     * @param cls 输出类型
     * @return 是否支持指定类型
     */
    Boolean isSupport(Class<?> cls);

    /**
     * 将输入数据转换为指定类型
     *
     * @param source 输入数据
     * @return 输出类型
     */
    T convert(Map<String, Object> source, Class<?> targetType);
}
