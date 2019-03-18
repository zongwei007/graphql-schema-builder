package com.ltsoft.graphql.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 指定某字段/方法在用作 Mutation 参数时的类型
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD})
@Inherited
public @interface GraphQLMutationType {

    Class<?> value();

}
