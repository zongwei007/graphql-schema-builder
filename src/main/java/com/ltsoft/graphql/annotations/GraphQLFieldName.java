package com.ltsoft.graphql.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 定义 GraphQL Field 名称格式化模板
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface GraphQLFieldName {

    /**
     * 格式化模板。应为 {@link BiFunction}&lt;&lt;String&gt;, Class&lt;?&gt;&gt; 的实现。传入参数为当前字段名和当前解析 Java 类。
     *
     * @return 格式化模板
     */
    Class<? extends BiFunction<String, Class<?>, String>> value();

}
