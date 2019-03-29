package com.ltsoft.graphql.resolver;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.GraphQLDirectiveBuilder;
import com.ltsoft.graphql.GraphQLNameProvider;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.DefaultDirectiveBuilder;
import graphql.introspection.Introspection;
import graphql.language.Type;
import graphql.language.*;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.ScalarInfo;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public final class ResolveUtil {

    private static final Pattern METHOD_NAME_PREFIX = Pattern.compile("^(is|get|set)([A-Z])");

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
    static <T extends Type> T wrapGraphQLType(TypeToken<?> typeToken, Function<Class<?>, ? extends Type> typeMapper, Boolean isNotNull) {
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

        Type result = typeMapper.apply(javaType);

        if (isList) {
            result = new ListType(result);
        }

        if (isNotNull) {
            result = new NonNullType(result);
        }

        return (T) result;
    }

    /**
     * 解析 GraphQL 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL 类型
     */
    static TypeName resolveType(Class<?> cls) {
        return TypeName.newTypeName()
                .comments(resolveComment(cls))
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();
    }

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

    public static String resolveFieldName(Class<?> resolvingCls, Method method, Field field) {
        if (resolvingCls.isAnnotationPresent(GraphQLNameFactory.class)) {
            Class<? extends GraphQLNameProvider> providerType = resolvingCls.getAnnotation(GraphQLNameFactory.class).value();
            try {
                GraphQLNameProvider provider = providerType.getConstructor().newInstance();
                String name = provider.provide(resolvingCls, method, field);

                return Strings.isNullOrEmpty(name) ? resolveFieldName(method, field) : name;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format("Can not construct GraphQLNameProvider '%s'", providerType.getName()), e);
            }
        }

        return resolveFieldName(method, field);
    }

    /**
     * 解析 GraphQL 字段名称
     *
     * @param method 需要解析的方法
     * @param field  方法匹配的字段
     * @return 字段名称
     */
    public static String resolveFieldName(Method method, Field field) {
        checkArgument(method != null || field != null);

        GraphQLName name = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLName.class))
                .orElseGet(() ->
                        Optional.ofNullable(method)
                                .map(ele -> ele.getAnnotation(GraphQLName.class))
                                .orElse(null)
                );

        if (name != null) {
            return name.value();
        }

        if (method != null) {
            return simplifyName(method.getName());
        }

        return field.getName();
    }

    /**
     * 解析 GraphQL 参数名称
     *
     * @param parameter 需要解析的参数
     * @return 参数名称
     */
    public static String resolveArgumentName(Parameter parameter) {
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
    static Description resolveDescription(Class<?> cls) {
        return Optional.ofNullable(cls.getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value)
                .map(str -> new Description(str, resolveSourceLocation(cls), str.contains("\n")))
                .orElse(null);
    }

    /**
     * 解析 GraphQL 字段描述
     *
     * @param method 所需要解析的方法
     * @param field  关联字段
     * @return 字段描述
     */
    static Description resolveDescription(Method method, Field field) {
        String description = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value)
                .orElseGet(() ->
                        Optional.ofNullable(method)
                                .map(ele -> ele.getAnnotation(GraphQLDescription.class))
                                .map(GraphQLDescription::value)
                                .orElse(null)
                );

        if (description != null) {
            return new Description(description, resolveSourceLocation(method, field), description.contains("\n"));
        }

        return null;
    }

    /**
     * 解析 GraphQL 参数描述
     *
     * @param parameter 所需解析的参数
     * @return 参数描述
     */
    static Description resolveDescription(Parameter parameter) {
        return Optional.ofNullable(parameter.getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value)
                .map(str -> new Description(str, null, str.contains("\n")))
                .orElse(null);
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
     * 解析实现的 GraphQL Interface
     *
     * @param cls 需要解析的类
     * @return GraphQL Interface 数组
     */
    @SuppressWarnings("UnstableApiUsage")
    static List<Type> resolveInterfaces(Class<?> cls) {
        return TypeToken.of(cls).getTypes()
                .interfaces()
                .stream()
                .map(TypeToken::getRawType)
                .filter(ele -> ele.isAnnotationPresent(GraphQLInterface.class))
                .map(ResolveUtil::resolveType)
                .collect(Collectors.toList());
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

    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults();

    @SuppressWarnings("UnstableApiUsage")
    static List<Comment> resolveComment(Class<?> cls) {
        return Optional.ofNullable(cls.getAnnotation(GraphQLComment.class))
                .map(GraphQLComment::value)
                .map(LINE_SPLITTER::splitToList)
                .map(list -> list.stream().map(ele -> new Comment(ele, resolveSourceLocation(cls))).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @SuppressWarnings("UnstableApiUsage")
    static List<Comment> resolveComment(Method method, Field field) {
        String comment = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLComment.class))
                .map(GraphQLComment::value)
                .orElseGet(() ->
                        Optional.ofNullable(method.getAnnotation(GraphQLComment.class))
                                .map(GraphQLComment::value)
                                .orElse(null)
                );

        if (comment != null) {
            return LINE_SPLITTER.splitToList(comment)
                    .stream()
                    .map(ele -> new Comment(ele, resolveSourceLocation(method, field)))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * 模拟 SourceLocation，仅记录类名
     *
     * @param cls 需要解析的类
     * @return SourceLocation 信息
     */
    static SourceLocation resolveSourceLocation(Class<?> cls) {
        return new SourceLocation(0, 0, cls.getName());
    }

    /**
     * 模拟 SourceLocation，记录类名和方法/字段名
     *
     * @param method 关联方法
     * @param field  同名字段
     * @return SourceLocation 信息
     */
    static SourceLocation resolveSourceLocation(Method method, Field field) {
        String name;

        if (field != null) {
            name = String.format("%s.%s", field.getDeclaringClass().getName(), field.getName());
        } else {
            name = String.format("%s#%s", method.getDeclaringClass().getName(), method.getName());
        }

        return new SourceLocation(0, 0, name);
    }

    /**
     * 检测 Java 类型是否按指定注解声明了 GraphQL 类型。支持 GraphQLTypeExtension
     *
     * @param cls             Java 类型
     * @param annotationClass 注解类型
     */
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
    static Optional<GraphQLScalarType> getStandardScalarType(String name) {
        return Optional.ofNullable(STANDARD_SCALAR_MAP.get(name));
    }

    /**
     * 判断某 Java 类型是否能被解析识别
     *
     * @param cls 要解析的类
     * @return 是否支持
     */
    public static boolean canResolve(Class<?> cls) {
        //抽象类不能作为解析入口
        if (!cls.isInterface() && Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }

        return cls.isAnnotationPresent(GraphQLType.class)
                || cls.isAnnotationPresent(GraphQLInterface.class)
                || cls.isAnnotationPresent(GraphQLInput.class)
                || cls.isAnnotationPresent(GraphQLTypeExtension.class)
                || cls.isAnnotationPresent(GraphQLUnion.class)
                || cls.isAnnotationPresent(GraphQLDirectiveLocations.class);
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
            result.put(resolveFieldName(null, fields[i]), constants[i]);
        }

        return result;
    }

    private static Map<ElementType, Introspection.DirectiveLocation> LOCATION_MAP = new HashMap<>();

    static {
        LOCATION_MAP.put(ElementType.TYPE, Introspection.DirectiveLocation.OBJECT);
        LOCATION_MAP.put(ElementType.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION);
        LOCATION_MAP.put(ElementType.METHOD, Introspection.DirectiveLocation.FIELD_DEFINITION);
        LOCATION_MAP.put(ElementType.PARAMETER, Introspection.DirectiveLocation.ARGUMENT_DEFINITION);
    }

    static List<DirectiveLocation> resolveDirectiveLocation(Class<?> cls) {
        Stream<Introspection.DirectiveLocation> locationStream = Stream.of();

        if (cls.isAnnotationPresent(GraphQLDirectiveLocations.class)) {
            locationStream = Arrays.stream(cls.getAnnotation(GraphQLDirectiveLocations.class).value());
        } else if (cls.isAnnotationPresent(Target.class)) {
            locationStream = Arrays.stream(cls.getAnnotation(Target.class).value())
                    .map(ele -> LOCATION_MAP.get(ele))
                    .filter(Objects::nonNull);
        }

        return locationStream.map(ele -> new DirectiveLocation(ele.name())).collect(Collectors.toList());
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

    private static List<Directive> resolveDirective(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .map(ResolveUtil::resolveDirective)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    static List<Directive> resolveDirective(Class<?> cls) {
        return resolveDirective(cls.getAnnotations());
    }

    static List<Directive> resolveDirective(Method method, Field field) {

        Stream<Annotation> fieldAnnotations = Optional.ofNullable(field)
                .map(ele -> Arrays.stream(ele.getAnnotations()))
                .orElse(Stream.of());

        Stream<Annotation> methodAnnotations = Optional.ofNullable(method)
                .map(ele -> Arrays.stream(ele.getAnnotations()))
                .orElse(Stream.of());

        return Stream.concat(fieldAnnotations, methodAnnotations)
                .map(ResolveUtil::resolveDirective)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    static List<Directive> resolveDirective(Parameter parameter) {
        return resolveDirective(parameter.getAnnotations());
    }
}
