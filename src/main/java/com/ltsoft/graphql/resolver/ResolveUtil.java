package com.ltsoft.graphql.resolver;

import com.google.common.base.Strings;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.GraphQLDirectiveBuilder;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.DefaultDirectiveBuilder;
import com.ltsoft.graphql.scalars.ScalarTypeRepository;
import graphql.language.Type;
import graphql.language.*;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.ScalarInfo;
import graphql.schema.idl.TypeInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public final class ResolveUtil {

    public static final SourceLocation EMPTY_SOURCE_LOCATION = new SourceLocation(0, 0);

    private static final Pattern METHOD_NAME_PREFIX = Pattern.compile("^(is|get|set)([A-Z])");

    /**
     * 解析 GraphQL 类型名称。支持 @GraphQLTypeExtension 注解。
     *
     * @param cls 需要解析的类
     * @return 类型名称
     */
    public static String resolveTypeName(Class<?> cls) {
        if (cls.isAnnotationPresent(GraphQLTypeExtension.class)) {
            return resolveTypeName(cls.getAnnotation(GraphQLTypeExtension.class).value());
        }

        return Optional.ofNullable(cls.getAnnotation(GraphQLName.class))
                .map(GraphQLName::value)
                .orElse(cls.getSimpleName());
    }

    /**
     * 解析 GraphQL 字段名称
     *
     * @param resolvingCls 当前解析类
     * @param method       需要解析的方法
     * @param field        方法匹配的字段
     * @return 字段名称
     */
    public static String resolveFieldName(Class<?> resolvingCls, Method method, Field field) {
        checkArgument(method != null || field != null);

        GraphQLName name = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLName.class))
                .orElseGet(() ->
                        Optional.ofNullable(method)
                                .map(ele -> ele.getAnnotation(GraphQLName.class))
                                .orElse(null)
                );

        String fieldName;

        if (name != null) {
            fieldName = name.value();
        } else if (method != null) {
            fieldName = simplifyName(method.getName());
        } else {
            fieldName = field.getName();
        }

        return Optional.ofNullable(method)
                .map(ele -> ele.getDeclaringClass().getAnnotation(GraphQLFieldName.class))
                .map(GraphQLFieldName::value)
                .map(type -> formatName(type, fieldName, resolvingCls))
                .orElse(fieldName);
    }

    @SuppressWarnings("WeakerAccess")
    public static Optional<String> resolveDescription(Class<?> cls) {
        return Optional.ofNullable(cls.getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value);
    }

    /**
     * 格式化 GraphQL 类型/字段名称
     *
     * @param formatterType 格式模板
     * @param name          类型/字段名称
     * @param javaType      字段所属 Java 类型
     * @return 格式化结果
     */
    static String formatName(Class<? extends BiFunction<String, Class<?>, String>> formatterType, String name, Class<?> javaType) {
        try {
            BiFunction<String, Class<?>, String> formatter = formatterType.getConstructor().newInstance();
            String result = formatter.apply(name, javaType);

            return Strings.isNullOrEmpty(result) ? name : result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Can not construct name formatter '%s'", formatterType.getName()), e);
        }
    }

    /**
     * 解析 GraphQL 参数名称。若未通过 {@link GraphQLArgument} 声明参数名称，则需要启用 JDK8+ 编译参数 -parameters。
     *
     * @param parameter 需要解析的参数
     * @return 参数名称
     */
    public static String resolveArgumentName(Parameter parameter) {
        return Optional.of(parameter.getAnnotation(com.ltsoft.graphql.annotations.GraphQLArgument.class))
                .map(com.ltsoft.graphql.annotations.GraphQLArgument::value)
                .filter(name -> !name.trim().isEmpty())
                .orElseGet(() ->
                        Optional.of(parameter)
                                .filter(Parameter::isNamePresent)
                                .map(Parameter::getName)
                                .orElse(null)
                );
    }

    static TypeName resolveTypeReference(GraphQLTypeReference annotation, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        String name = Optional.of(annotation.type())
                .filter(type -> !Object.class.equals(type))
                .map(resolver::apply)
                .map(TypeProvider::getTypeName)
                .orElse(annotation.name());

        return Strings.isNullOrEmpty(name) ? null : new TypeName(name);
    }

    static Type replaceTypeName(Type inputType, TypeName replace) {
        if (inputType instanceof NonNullType) {
            NonNullType nonNullType = (NonNullType) inputType;

            return nonNullType.transform(ele -> ele.type(replaceTypeName(nonNullType.getType(), replace)));
        }

        if (inputType instanceof ListType) {
            ListType listType = (ListType) inputType;

            return listType.transform(ele -> ele.type(replaceTypeName(listType.getType(), replace)));
        }

        if (inputType instanceof TypeName) {
            return replace;
        }

        return inputType;
    }

    /**
     * 将 getter 方法名转换为 fieldName
     *
     * @param name 方法名称
     * @return fieldName
     */
    @SuppressWarnings("WeakerAccess")
    public static String simplifyName(String name) {
        String fieldName = METHOD_NAME_PREFIX.matcher(name).replaceFirst("$2");

        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    /**
     * 检测 Java 类型是否按指定注解声明了 GraphQL 类型。支持 GraphQLTypeExtension
     *
     * @param cls             Java 类型
     * @param annotationClass 注解类型
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean hasGraphQLAnnotation(Class<?> cls, Class<? extends Annotation> annotationClass) {
        boolean isExtensionType = cls.isAnnotationPresent(GraphQLTypeExtension.class) && cls.getAnnotation(GraphQLTypeExtension.class).value().isAnnotationPresent(annotationClass);

        return cls.isAnnotationPresent(annotationClass) || isExtensionType;
    }

    private static Map<String, GraphQLScalarType> STANDARD_SCALAR_MAP;

    static {
        STANDARD_SCALAR_MAP = ScalarInfo.STANDARD_SCALARS.stream()
                .collect(Collectors.toMap(GraphQLScalarType::getName, Function.identity()));
    }

    /**
     * 按名称获取 GraphQL Java 自带的基本类型
     *
     * @param name 类型名称
     * @return GraphQL Scalar 类型
     */
    @SuppressWarnings("WeakerAccess")
    public static Optional<GraphQLScalarType> getStandardScalarType(String name) {
        return Optional.ofNullable(STANDARD_SCALAR_MAP.get(name));
    }

    /**
     * 判断某 Java 类型是否能被解析识别
     *
     * @param cls 要解析的类
     * @return 是否支持
     */
    public static boolean canResolve(Class<?> cls) {
        return cls.isAnnotationPresent(GraphQLType.class)
                || cls.isAnnotationPresent(GraphQLInterface.class)
                || cls.isAnnotationPresent(GraphQLInput.class)
                || cls.isAnnotationPresent(GraphQLTypeExtension.class)
                || cls.isAnnotationPresent(GraphQLUnion.class)
                || cls.isAnnotationPresent(GraphQLDirective.class);
    }

    /**
     * 解析泛型类型
     *
     * @param resolvingCls 定义有泛型信息的类
     * @param type         解析类型
     * @return 实际类型
     */
    @SuppressWarnings("UnstableApiUsage")
    public static TypeToken<?> resolveGenericType(Class<?> resolvingCls, java.lang.reflect.Type type) {
        TypeToken<?> typeToken = TypeToken.of(type);
        TypeToken<?> resolvingTypeToken = TypeToken.of(resolvingCls);

        if (type instanceof TypeVariable || typeToken.getRawType().getTypeParameters().length > 0) {
            return resolvingTypeToken.resolveType(typeToken.getType());
        }

        return typeToken;
    }

    /**
     * 解析枚举类型的值映射
     *
     * @param type 枚举类
     * @return 枚举值的映射信息
     */
    public static HashBiMap<String, Object> resolveEnumValueMap(Class<?> type) {
        Field[] fields = type.getFields();
        Object[] constants = type.getEnumConstants();
        HashBiMap<String, Object> result = HashBiMap.create(constants.length);

        for (int i = 0, len = constants.length; i < len; i++) {
            result.put(resolveFieldName(type, null, fields[i]), constants[i]);
        }

        return result;
    }

    private static final List<GraphQLDirectiveBuilder> DIRECTIVE_RESOLVERS = new ArrayList<>();

    static {
        DIRECTIVE_RESOLVERS.add(new DefaultDirectiveBuilder());
    }

    /**
     * 注册指令解析器
     *
     * @param resolvers 指令解析器
     */
    @SuppressWarnings("unused")
    public static void register(GraphQLDirectiveBuilder... resolvers) {
        Collections.addAll(DIRECTIVE_RESOLVERS, resolvers);
    }

    private static Directive resolveDirective(Annotation annotation) {
        //noinspection unchecked
        return DIRECTIVE_RESOLVERS.stream()
                .filter(ele -> ele.isSupport(annotation.annotationType()))
                .findFirst()
                .map(ele -> ele.builder(annotation).build())
                .orElse(null);
    }

    static Stream<FieldInformation> resolveFields(Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .filter(ResolveUtil::isInvokable)
                .map(method -> new FieldInformation(cls, method))
                .filter(FieldInformation::isField);
    }

    static boolean hasReturnType(Method method) {
        if (method == null) {
            return false;
        }

        return !void.class.equals(method.getReturnType()) && !Void.class.equals(method.getReturnType());
    }

    private static boolean isInvokable(Method method) {
        //noinspection UnstableApiUsage
        Invokable<?, Object> invokable = Invokable.from(method);

        return !invokable.isStatic() && invokable.isPublic() && !method.isBridge();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static boolean isGraphQLList(TypeToken<?> typeToken) {
        return typeToken.isArray() || typeToken.isSubtypeOf(Collection.class);
    }

    static Value resolveInputDefaultValue(GraphQLDefaultValue defaultValue, Type inputType, Boolean isEnum) {
        return Optional.ofNullable(defaultValue)
                .map(GraphQLDefaultValue::value)
                .map(str -> {
                    if (isEnum) {
                        return new EnumValue(str);
                    }

                    return Optional.of(TypeInfo.typeInfo(inputType).getName())
                            .map(name -> ScalarTypeRepository.getInstance().getScalarType(name)
                                    .orElseGet(() -> getStandardScalarType(name).orElse(null)))
                            .map(scalarType -> AstValueHelper.astFromValue(str, scalarType))
                            .orElse(null);
                })
                .orElse(null);
    }

    static Directive resolveDirective(Annotation annotation, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        Directive directive = resolveDirective(annotation);

        if (directive != null) {
            //仅用于注册注解类型，不需要处理返回值
            resolver.apply(annotation.annotationType());
        }

        return directive;
    }

    static List<Directive> resolveDirective(Annotation[] annotations, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        return Arrays.stream(annotations)
                .map(ele -> resolveDirective(ele, resolver))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("UnstableApiUsage")
    static Type resolveGraphQLType(TypeToken<?> typeToken, Function<java.lang.reflect.Type, TypeProvider<?>> resolver, Boolean isNotNull) {
        boolean isArray = isGraphQLList(typeToken);
        TypeToken<?> unwrapped = typeToken;

        if (isArray) {
            unwrapped = unwrapListType(typeToken);
        }

        TypeProvider<?> provider = resolver.apply(unwrapped.getType());
        Type result = new TypeName(provider.getTypeName());

        //重新包装
        if (isArray) {
            result = new ListType(result);
        }

        if (isNotNull) {
            result = new NonNullType(result);
        }

        return result;
    }

    @SuppressWarnings({"UnstableApiUsage", "WeakerAccess"})
    public static TypeToken<?> unwrapListType(TypeToken<?> typeToken) {
        if (isGraphQLList(typeToken)) {
            if (typeToken.getComponentType() != null) {
                return typeToken.getComponentType();
            } else {
                TypeVariable<? extends Class<?>>[] typeParameters = typeToken.getRawType().getTypeParameters();

                checkState(typeParameters.length == 1);

                return typeToken.resolveType(typeParameters[0]);
            }
        } else {
            return typeToken;
        }
    }

    static boolean isGraphQLEnumType(Class<?> type) {
        return type.isEnum() && hasGraphQLAnnotation(type, GraphQLType.class);
    }

    static boolean isGraphQLObjectLikeType(Class<?> cls) {
        return hasGraphQLAnnotation(cls, GraphQLType.class) || hasGraphQLAnnotation(cls, GraphQLInterface.class) || hasGraphQLAnnotation(cls, GraphQLInput.class);
    }

    static boolean isGraphQLInputType(Class<?> paramType) {
        return isGraphQLEnumType(paramType) || hasGraphQLAnnotation(paramType, GraphQLInput.class) || !isGraphQLObjectLikeType(paramType);
    }

    static boolean isGraphQLExtensionType(Class<?> cls) {
        return cls.isAnnotationPresent(GraphQLTypeExtension.class);
    }
}
