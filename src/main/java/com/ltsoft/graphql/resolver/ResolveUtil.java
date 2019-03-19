package com.ltsoft.graphql.resolver;

import com.google.common.base.CaseFormat;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.annotations.*;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

public class ResolveUtil {

    private static Pattern METHOD_NAME_PREFIX = Pattern.compile("^(is|get|set)([A-Z])");

    /**
     * 包装 GraphQL 类型。自动对 Java 类型进行拆包，判断是否为 List 类型并重新封装。
     *
     * @param typeToken  Java 类型
     * @param typeMapper GraphQL 基础类型提供者
     * @param isNotNull  是否要求非空
     * @param <T>        GraphQL 类型
     * @return GraphQL Type
     */
    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    static <T extends GraphQLType> T wrapGraphQLType(TypeToken<?> typeToken, Function<Class<?>, ? extends GraphQLType> typeMapper, Boolean isNotNull) {
        boolean isList = typeToken.isArray() || typeToken.isSubtypeOf(Iterable.class);
        Class<?> javaType = typeToken.getRawType();

        if (isList) {
            if (typeToken.getComponentType() != null) {
                javaType = typeToken.getComponentType().getRawType();
            } else {
                TypeVariable<? extends Class<?>>[] typeParameters = typeToken.getRawType().getTypeParameters();

                checkState(typeParameters.length == 1);

                javaType = typeToken.resolveType(typeParameters[0]).getRawType();
            }
        }

        GraphQLType result = typeMapper.apply(javaType);

        if (isList) {
            result = GraphQLList.list(result);
        }

        if (isNotNull) {
            result = GraphQLNonNull.nonNull(result);
        }

        return (T) result;
    }

    /**
     * 解析 GraphQL 类型名称
     *
     * @param cls 需要解析的类
     * @return 类型名称
     */
    static String resolveTypeName(Class<?> cls) {
        return Optional.ofNullable(cls.getAnnotation(GraphQLName.class))
                .map(GraphQLName::value)
                .orElse(cls.getSimpleName());
    }

    /**
     * 解析 GraphQL 字段名称
     *
     * @param method 需要解析的方法
     * @param field  方法匹配的字段
     * @return 字段名称
     */
    static String resolveFieldName(Method method, Field field) {
        return Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLName.class))
                .map(GraphQLName::value)
                .orElseGet(() ->
                        Optional.ofNullable(method.getAnnotation(GraphQLName.class))
                                .map(GraphQLName::value)
                                .orElse(simplifyName(method.getName()))
                );
    }

    /**
     * 解析 GraphQL 参数名称
     *
     * @param parameter 需要解析的参数
     * @return 参数名称
     */
    static String resolveArgumentName(Parameter parameter) {
        return Optional.of(parameter.getAnnotation(com.ltsoft.graphql.annotations.GraphQLArgument.class))
                .map(com.ltsoft.graphql.annotations.GraphQLArgument::value)
                .filter(name -> !name.trim().isEmpty())
                .orElse(parameter.getName());
    }

    /**
     * 解析 GraphQL 类型描述
     *
     * @param cls 需要解析的类型
     * @return 类型描述
     */
    static String resolveTypeDescription(Class<?> cls) {
        return Optional.ofNullable(cls.getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value)
                .orElse(null);
    }

    /**
     * 解析 GraphQL 字段描述
     *
     * @param method 所需要解析的方法
     * @param field  关联字段
     * @return 字段描述
     */
    static String resolveFieldDescription(Method method, Field field) {
        return Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value)
                .orElseGet(() ->
                        Optional.ofNullable(method.getAnnotation(GraphQLDescription.class))
                                .map(GraphQLDescription::value)
                                .orElse(null)
                );
    }

    /**
     * 解析 GraphQL 参数描述
     *
     * @param parameter 所需解析的参数
     * @return 参数描述
     */
    static String resolveArgumentDescription(Parameter parameter) {
        return Optional.ofNullable(parameter.getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value)
                .orElse(null);
    }

    /**
     * 解析 GraphQL 字段作废原因
     *
     * @param method 所需要解析的方法
     * @param field  关联字段
     * @return 作废原因
     */
    static String resolveFieldDeprecate(Method method, Field field) {
        return Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLDeprecate.class))
                .map(GraphQLDeprecate::value)
                .orElseGet(() ->
                        Optional.ofNullable(method.getAnnotation(GraphQLDeprecate.class))
                                .map(GraphQLDeprecate::value)
                                .orElse(null)
                );
    }

    /**
     * 判断 GraphQLIgnore 注解是否生效
     *
     * @param ignore 注解声明
     * @param views  当前 GraphQLView 上下文
     * @return 是否已忽略
     */
    static boolean isIgnore(GraphQLIgnore ignore, Class<?>[] views) {
        return Optional.ofNullable(ignore)
                .filter(ele -> ele.view().length == 0 || Arrays.stream(views).anyMatch(view -> Arrays.stream(ele.view()).anyMatch(view::isAssignableFrom)))
                .isPresent();
    }

    /**
     * 判断 GraphQLNotNull 注解是否生效
     *
     * @param notNull 注解声明
     * @param views   当前 GraphQLView 上下文
     * @return 是否不为空
     */
    static boolean isNotNull(GraphQLNotNull notNull, Class<?>[] views) {
        return Optional.ofNullable(notNull)
                .filter(ele -> ele.view().length == 0 || Arrays.stream(views).anyMatch(view -> Arrays.stream(ele.view()).anyMatch(view::isAssignableFrom)))
                .isPresent();
    }

    /**
     * 将 getter 方法名转换为 fieldName
     *
     * @param name 方法名称
     * @return fieldName
     */
    static String simplifyName(String name) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, METHOD_NAME_PREFIX.matcher(name).replaceFirst("$2"));
    }

    /**
     * 解析实现的 GraphQL Interface
     *
     * @param cls 需要解析的类
     * @return GraphQL Interface 数组
     */
    @SuppressWarnings("UnstableApiUsage")
    static GraphQLTypeReference[] resolveInterfaces(Class<?> cls) {
        return TypeToken.of(cls).getTypes()
                .interfaces()
                .stream()
                .map(TypeToken::getRawType)
                .filter(ele -> ele.isAnnotationPresent(GraphQLInterface.class))
                .map(ele -> GraphQLTypeReference.typeRef(resolveTypeName(ele)))
                .toArray(GraphQLTypeReference[]::new);
    }
}
