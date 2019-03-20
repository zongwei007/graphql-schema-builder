package com.ltsoft.graphql.resolver;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ServiceInstanceFactory;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.types.ScalarTypeRepository;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLType;
import graphql.schema.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;

@SuppressWarnings("UnstableApiUsage")
public class SchemaResolver {

    private static Pattern SETTER_PREFIX = Pattern.compile("^set[A-Z]?\\S*");

    private final ServiceInstanceFactory instanceFactory;
    private final ScalarTypeRepository typeRepository = new ScalarTypeRepository();
    private final Map<Class<?>, Set<Class<?>>> typeExtensions = new HashMap<>();

    public SchemaResolver() {
        this(new DefaultServiceInstanceFactory());
    }

    public SchemaResolver(ServiceInstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    public SchemaResolver scalar(Class<? extends GraphQLScalarType> cls, Class<?> javaType) {
        checkArgument(GraphQLScalarType.class.isAssignableFrom(cls));
        GraphQLScalarType provide = instanceFactory.provide(cls);

        return scalar(provide, javaType);
    }

    public SchemaResolver scalar(GraphQLScalarType scalarType, Class<?> javaType) {
        typeRepository.mapping(javaType, scalarType);
        return this;
    }

    public SchemaResolver extension(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(GraphQLTypeExtension.class));

        Class<?> targetClass = cls.getAnnotation(GraphQLTypeExtension.class).value();
        typeExtensions.computeIfAbsent(targetClass, key -> new HashSet<>()).add(cls);

        return this;
    }

    /**
     * 解析 GraphQL Object 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Object
     */
    public GraphQLObjectType object(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class));

        return GraphQLObjectType.newObject()
                .name(resolveTypeName(cls))
                .description(resolveTypeDescription(cls))
                .fields(resolveFields(cls))
                .withInterfaces(resolveInterfaces(cls))
                .build();
    }

    /**
     * 解析 GraphQL Input 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Input
     */
    public GraphQLInputObjectType input(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(GraphQLInput.class));

        return GraphQLInputObjectType.newInputObject()
                .name(resolveTypeName(cls))
                .description(resolveTypeDescription(cls))
                .fields(resolveInputFields(cls))
                .build();
    }

    /**
     * 解析 GraphQL Interface 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Interface
     */
    public GraphQLInterfaceType iface(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(GraphQLInterface.class));

        TypeResolver typeResolver = Optional.of(cls.getAnnotation(GraphQLInterface.class))
                .map(GraphQLInterface::typeResolver)
                .map(instanceFactory::provide)
                .get();

        List<GraphQLFieldDefinition> fields = TypeToken.of(cls).getTypes().interfaces().stream()
                .map(TypeToken::getRawType)
                .filter(ele -> ele.isAnnotationPresent(GraphQLInterface.class))
                .flatMap(ele -> Arrays.stream(ele.getMethods())
                        .filter(method -> !SETTER_PREFIX.matcher(method.getName()).matches())    //忽略 setter
                        .filter(method -> method.getDeclaringClass().equals(ele))   //仅识别类型自身方法
                        .map(method -> resolveField(method, null)))
                .collect(Collectors.toList());

        return GraphQLInterfaceType.newInterface()
                .name(resolveTypeName(cls))
                .description(resolveTypeDescription(cls))
                .fields(fields)
                .typeResolver(typeResolver)
                .build();
    }

    /**
     * 解析 GraphQL Union 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Union
     */
    public GraphQLUnionType union(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(GraphQLUnion.class));

        TypeResolver typeResolver = Optional.of(cls.getAnnotation(GraphQLUnion.class))
                .map(GraphQLUnion::typeResolver)
                .map(instanceFactory::provide)
                .get();

        GraphQLTypeReference[] possibleTypes = Arrays.stream(cls.getAnnotation(GraphQLUnion.class).possibleTypes())
                .map(ResolveUtil::resolveTypeName)
                .map(GraphQLTypeReference::typeRef)
                .toArray(GraphQLTypeReference[]::new);

        return GraphQLUnionType.newUnionType()
                .name(resolveTypeName(cls))
                .description(resolveTypeDescription(cls))
                .possibleTypes(possibleTypes)
                .typeResolver(typeResolver)
                .build();
    }

    /**
     * 解析 GraphQL Enum 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Enum
     */
    public GraphQLEnumType enumeration(Class<?> cls) {
        checkArgument(cls.isEnum());
        checkArgument(cls.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class));

        GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                .name(resolveTypeName(cls))
                .description(resolveTypeDescription(cls));

        Field[] fields = cls.getFields();
        Object[] constants = cls.getEnumConstants();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Object value = constants[i];
            String description = Optional.ofNullable(field.getAnnotation(GraphQLDescription.class))
                    .map(GraphQLDescription::value)
                    .orElse(null);
            String deprecate = Optional.ofNullable(field.getAnnotation(GraphQLDeprecate.class))
                    .map(GraphQLDeprecate::value)
                    .orElse(null);

            builder.value(field.getName(), value, description, deprecate);
        }

        return builder.build();
    }

    /**
     * 获取 Scalar Type 容器
     *
     * @return Scalar Type 容器
     */
    public ScalarTypeRepository getTypeRepository() {
        return typeRepository;
    }

    /**
     * 解析 GraphQL 字段描述
     *
     * @param cls 需要解析的类
     * @return 字段描述
     */
    private List<GraphQLFieldDefinition> resolveFields(Class<?> cls) {
        // field 不支持 ignore
        return resolveClassExtensions(TypeToken.of(cls), ele -> ele.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class))
                .flatMap(ele -> {
                    Map<String, Field> fieldNameMap = Arrays.stream(ele.getDeclaredFields())
                            .collect(Collectors.toMap(Field::getName, Function.identity()));

                    return Arrays.stream(ele.getMethods())
                            .filter(method -> !SETTER_PREFIX.matcher(method.getName()).matches())    //忽略 setter
                            .filter(method -> method.getDeclaringClass().equals(ele))   //仅识别类型自身方法
                            .map(method -> resolveField(method, fieldNameMap.get(simplifyName(method.getName()))));
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析 GraphQL Field
     *
     * @param method 字段关联的方法
     * @param field  字段同名的属性
     * @return GraphQL Field 定义
     */
    private GraphQLFieldDefinition resolveField(Method method, Field field) {
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(resolveFieldName(method, field))
                .description(resolveFieldDescription(method, field))
                .deprecate(resolveFieldDeprecate(method, field))
                .argument(resolveFieldArguments(method))
                .type(resolveFieldType(method, field))
                .build();
    }

    /**
     * 解析 GraphQL Field 类型
     *
     * @param method 字段关联的方法
     * @param field  字段同名的属性
     * @return GraphQL Field 类型
     */
    private GraphQLOutputType resolveFieldType(Method method, Field field) {
        Invokable<?, Object> invokable = Invokable.from(method);

        boolean isNotNull = Optional.ofNullable(field)
                .map(ele -> ele.isAnnotationPresent(GraphQLNotNull.class))
                .orElse(invokable.isAnnotationPresent(GraphQLNotNull.class));

        return wrapGraphQLType(invokable.getReturnType(), cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> (GraphQLOutputType) ele)
                //若无法识别为 ScalarType，则使用类型引用作为 OutputType
                .orElse(GraphQLTypeReference.typeRef(resolveTypeName(cls))), isNotNull);
    }

    /**
     * 解析 GraphQL Field Arguments
     *
     * @param method 字段关联的方法
     * @return GraphQL Field Arguments
     */
    private List<GraphQLArgument> resolveFieldArguments(Method method) {
        Class<?>[] views = Optional.ofNullable(method.getAnnotation(GraphQLView.class))
                .map(GraphQLView::value)
                .orElse(new Class[0]);

        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLArgument.class))
                .flatMap(parameter -> resolveFieldArgument(parameter, views))
                .collect(Collectors.toList());
    }

    /**
     * 解析 GraphQL Argument
     *
     * @param parameter 关联的参数
     * @param views     当前 GraphQLView 信息
     * @return GraphQL Argument
     */
    private Stream<GraphQLArgument> resolveFieldArgument(Parameter parameter, Class<?>[] views) {
        GraphQLInputType inputType = resolveArgumentType(parameter, views);

        if (inputType != null) {
            GraphQLArgument argument = GraphQLArgument.newArgument()
                    .name(resolveArgumentName(parameter))
                    .description(resolveArgumentDescription(parameter))
                    .type(inputType)
                    .defaultValue(resolveArgumentDefaultValue(parameter, GraphQLTypeUtil.unwrapNonNull(inputType)))
                    .build();

            return Stream.of(argument);
        } else {
            return resolveArgumentGroup(parameter, views);
        }
    }

    /**
     * 解析 GraphQL Argument 类型
     *
     * @param parameter 关联的参数
     * @param views     当前 GraphQLView 信息
     * @return GraphQL Argument 类型
     */
    private GraphQLInputType resolveArgumentType(Parameter parameter, Class<?>[] views) {
        boolean isNotNull = isNotNull(parameter.getAnnotation(GraphQLNotNull.class), views);
        TypeToken<?> typeToken = TypeToken.of(parameter.getParameterizedType());

        return wrapGraphQLType(typeToken, cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> (GraphQLInputType) ele)
                .orElseGet(() -> {
                    if (typeToken.getRawType().isAnnotationPresent(GraphQLInput.class)) {
                        return GraphQLTypeReference.typeRef(resolveTypeName(typeToken.getRawType()));
                    }

                    return null;
                }), isNotNull);
    }

    /**
     * 将输入参数无法识别为 GraphQL Input 的 Java 对象的 Field 参数进行拆解，转换为一组 GraphQL Argument
     *
     * @param parameter 关联的参数
     * @param views     当前 GraphQLView 信息
     * @return GraphQL Field Arguments
     */
    private Stream<GraphQLArgument> resolveArgumentGroup(Parameter parameter, Class<?>[] views) {
        return resolveClassExtensions(TypeToken.of(parameter.getParameterizedType()), ele -> ele.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class))
                .flatMap(ele -> {
                    Map<String, Field> fieldNameMap = Arrays.stream(ele.getDeclaredFields())
                            .collect(Collectors.toMap(Field::getName, Function.identity()));

                    return Arrays.stream(ele.getMethods())
                            .filter(method -> method.getParameterCount() > 0)
                            .filter(method -> SETTER_PREFIX.matcher(method.getName()).matches())    //仅使用 setter
                            .filter(method -> method.getDeclaringClass().equals(ele))   //仅识别类型自身方法
                            .flatMap(method -> resolveFieldAsArgument(method, fieldNameMap.get(simplifyName(method.getName())), views));
                });
    }

    /**
     * 将近似于 GraphQL Object 的 Java 对象的方法解析为输入参数
     *
     * @param method 关联的方法
     * @param field  同名的字段
     * @param views  当前 GraphQLView 信息
     * @return GraphQL Field Arguments
     */
    private Stream<GraphQLArgument> resolveFieldAsArgument(Method method, Field field, Class<?>[] views) {
        Parameter parameter = method.getParameters()[0];
        GraphQLIgnore ignore = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLIgnore.class))
                .orElse(method.getAnnotation(GraphQLIgnore.class));
        GraphQLNotNull notNull = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLNotNull.class))
                .orElse(parameter.getAnnotation(GraphQLNotNull.class));
        GraphQLMutationType mutationType = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLMutationType.class))
                .orElse(parameter.getAnnotation(GraphQLMutationType.class));

        if (!isIgnore(ignore, views)) {
            TypeToken<?> paramType = TypeToken.of(parameter.getParameterizedType());

            if (field != null && paramType.isSupertypeOf(field.getGenericType())) {
                paramType = TypeToken.of(field.getGenericType());
            }

            if (mutationType != null) {
                paramType = TypeToken.of(mutationType.value());
            }

            TypeToken<?> typeToken = paramType;

            GraphQLInputType inputType = wrapGraphQLType(typeToken, cls -> typeRepository.findMappingScalarType(cls)
                    .map(ele -> (GraphQLInputType) ele)
                    .orElseGet(() -> {
                        if (typeToken.getRawType().isAnnotationPresent(GraphQLInput.class)) {
                            return GraphQLTypeReference.typeRef(resolveTypeName(typeToken.getRawType()));
                        }

                        return null;
                    }), isNotNull(notNull, views));

            if (inputType != null) {
                GraphQLArgument argument = GraphQLArgument.newArgument()
                        .name(resolveFieldName(method, field))
                        .description(resolveFieldDescription(method, field))
                        .type(inputType)
                        .defaultValue(resolveFieldDefaultValue(method, field, GraphQLTypeUtil.unwrapNonNull(inputType)))
                        .build();

                return Stream.of(argument);
            }
        }

        return Stream.of();
    }

    /**
     * 解析参数默认值
     *
     * @param parameter 关联参数
     * @param type      参数类型
     * @return 参数默认值
     */
    private Object resolveArgumentDefaultValue(Parameter parameter, GraphQLType type) {
        return Optional.ofNullable(parameter.getAnnotation(GraphQLDefaultValue.class))
                .map(GraphQLDefaultValue::value)
                .map(str -> {
                    if (type instanceof GraphQLScalarType) {
                        return ((GraphQLScalarType) type).getCoercing().parseValue(str);
                    }

                    return str;
                })
                .orElseGet(() ->
                        Optional.ofNullable(parameter.getAnnotation(GraphQLDefaultValueFactory.class))
                                .map(GraphQLDefaultValueFactory::value)
                                .map(cls -> instanceFactory.provide(cls).get())
                                .orElse(null)
                );
    }

    /**
     * 字段映射为参数时，解析参数默认值
     *
     * @param method 关联的方法
     * @param field  同名的字段
     * @param type   字段类型
     * @return 字段默认值
     */
    private Object resolveFieldDefaultValue(Method method, Field field, GraphQLType type) {
        return Optional.ofNullable(
                Optional.ofNullable(field)
                        .map(ele -> ele.getAnnotation(GraphQLDefaultValue.class))
                        .map(GraphQLDefaultValue::value)
                        .orElseGet(() ->
                                Optional.ofNullable(method.getAnnotation(GraphQLDefaultValue.class))
                                        .map(GraphQLDefaultValue::value)
                                        .orElse(null)
                        )
        ).map(str -> {
            if (type instanceof GraphQLScalarType) {
                return ((GraphQLScalarType) type).getCoercing().parseValue(str);
            }

            return str;
        }).orElseGet(() -> {
            Class<? extends Supplier<?>> fieldFactory = Optional.ofNullable(field)
                    .map(ele -> ele.getAnnotation(GraphQLDefaultValueFactory.class))
                    .map(GraphQLDefaultValueFactory::value)
                    .orElse(null);

            Class<? extends Supplier<?>> methodFactory = Optional.ofNullable(method.getAnnotation(GraphQLDefaultValueFactory.class))
                    .map(GraphQLDefaultValueFactory::value)
                    .orElse(null);

            Class<? extends Supplier<?>> factory = null;
            // 这里不能用 Optional.ofNullable(fieldFactory).orElse(methodFactory)，会引起编译错误
            if (fieldFactory != null) {
                factory = fieldFactory;
            } else if (methodFactory != null) {
                factory = methodFactory;
            }

            if (factory != null) {
                return instanceFactory.provide(factory).get();
            }

            return null;
        });
    }

    /**
     * 解析 GraphQL Input Object 字段描述
     *
     * @param cls 关联的类
     * @return GraphQL Input Object 字段描述
     */
    private List<GraphQLInputObjectField> resolveInputFields(Class<?> cls) {
        return resolveClassExtensions(TypeToken.of(cls), ele -> ele.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class) || ele.isAnnotationPresent(GraphQLInterface.class))
                .flatMap(ele -> {
                    Map<String, Field> fieldNameMap = Arrays.stream(ele.getDeclaredFields())
                            .collect(Collectors.toMap(Field::getName, Function.identity()));

                    // field 不支持 ignore

                    return Arrays.stream(ele.getMethods())
                            .filter(method -> SETTER_PREFIX.matcher(method.getName()).matches())    //仅使用 setter
                            .filter(method -> method.getDeclaringClass().equals(ele))   //仅识别类型自身方法;
                            .map(method -> resolveInputField(method, fieldNameMap.get(simplifyName(method.getName()))));
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析 GraphQL Input Object 字段描述
     *
     * @param method 关联的方法
     * @param field  同名字段
     * @return Input Object Field 描述
     */
    private GraphQLInputObjectField resolveInputField(Method method, Field field) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(resolveFieldName(method, field))
                .description(resolveFieldDescription(method, field))
                .type(resolveInputFieldType(method, field))
                .build();
    }

    /**
     * 解析 Object 所有的扩展类型
     *
     * @param typeToken Object 类型信息
     * @return 所有扩展类型信息
     */
    private Stream<Class<?>> resolveClassExtensions(TypeToken<?> typeToken, Predicate<Class<?>> filter) {
        Stream<Class<?>> objectExtensions = typeToken.getTypes().stream()
                .map(TypeToken::getRawType)
                .flatMap(ele -> typeExtensions.getOrDefault(ele, Collections.emptySet()).stream());

        Stream<? extends Class<?>> withParents = typeToken.getTypes().stream()
                .map(TypeToken::getRawType)
                .filter(filter);

        Class<?>[] fieldExtensions = Optional.ofNullable(typeToken.getRawType().getAnnotation(GraphQLFieldExtension.class))
                .map(GraphQLFieldExtension::value)
                .orElse(new Class[0]);

        return Stream.concat(objectExtensions, Stream.concat(withParents, Arrays.stream(fieldExtensions)));
    }

    private GraphQLInputType resolveInputFieldType(Method method, Field field) {
        Invokable<?, Object> invokable = Invokable.from(method);

        boolean isNotNull = Optional.ofNullable(field)
                .map(ele -> ele.isAnnotationPresent(GraphQLNotNull.class))
                .orElse(invokable.isAnnotationPresent(GraphQLNotNull.class));

        return wrapGraphQLType(invokable.getReturnType(), cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> (GraphQLInputType) ele)
                //若无法识别为 ScalarType，则使用类型引用作为 InputType
                .orElse(GraphQLTypeReference.typeRef(resolveTypeName(cls))), isNotNull);
    }

}
