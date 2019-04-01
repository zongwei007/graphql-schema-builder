package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明一个 GraphQL 类型引用。
 * 如声明了该注解，则会按注解描述定义 GraphQL 字段/参数的类型。
 * 如果与 {@link GraphQLMutationType} 同时使用，{@link GraphQLMutationType} 优先。
 *
 * @see GraphQLMutationType
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, METHOD, PARAMETER})
@Inherited
public @interface GraphQLTypeReference {

    /**
     * 使用类型名称声明类型引用
     *
     * @return 引用类型名称
     */
    String name() default "";

    /**
     * 直接声明替代类型
     *
     * @return 替代类型
     */
    Class<?> type() default Object.class;

}
