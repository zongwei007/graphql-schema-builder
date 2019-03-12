package com.ltsoft.graphql.resolver;

import com.google.common.base.CaseFormat;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.types.ScalarTypeRepository;
import graphql.schema.GraphQLArgument;
import graphql.schema.*;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public class SchemaResolver {

    private static Pattern GETTER_PREFIX = Pattern.compile("^(is|get)([A-Z])");

    private static Pattern SETTER_PREFIX = Pattern.compile("^set[A-Z]?\\S*");

    private final ScalarTypeRepository typeRepository;

    public SchemaResolver() {
        this(new ScalarTypeRepository());
    }

    public SchemaResolver(ScalarTypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    /**
     * 解析 GraphQL Object 类型
     *
     * @param cls 需要解析的类型
     * @return GraphQL Object 类型
     */
    public GraphQLObjectType object(Class<?> cls) {
        return GraphQLObjectType.newObject()
                .name(resolveTypeName(cls))
                .description(resolveTypeDescription(cls))
                .fields(resolveFields(cls))
                .build();
    }


    /**
     * 解析 GraphQL 字段描述
     *
     * @param cls 需要解析的类型
     * @return 字段描述
     */
    private List<GraphQLFieldDefinition> resolveFields(Class<?> cls) {

        Map<String, Field> fieldNameMap = Arrays.stream(cls.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, Function.identity()));

        //TODO 类型扩展支持

        return Arrays.stream(cls.getMethods())
                .filter(method -> !SETTER_PREFIX.matcher(method.getName()).matches())    //忽略 setter
                .filter(method -> method.getDeclaringClass().equals(cls))   //仅识别类型自身方法
                .map(method -> resolveField(method, fieldNameMap.get(simplifyName(method.getName()))))
                .collect(Collectors.toList());
    }

    /**
     * 解析字段
     *
     * @param method
     * @param field
     * @return
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


    @SuppressWarnings("UnstableApiUsage")
    private GraphQLOutputType resolveFieldType(Method method, Field field) {
        Invokable<?, Object> invokable = Invokable.from(method);

        boolean isNotNull = Optional.ofNullable(field)
                .map(ele -> ele.isAnnotationPresent(NotNull.class))
                .orElse(invokable.isAnnotationPresent(NotNull.class));

        GraphQLOutputType outputType = resolveGraphQLType(invokable.getReturnType(), cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> (GraphQLOutputType) ele)
                //若无法识别为 ScalarType，则使用类型引用作为 OutputType
                .orElse(GraphQLTypeReference.typeRef(resolveTypeName(cls))));

        if (isNotNull) {
            outputType = GraphQLNonNull.nonNull(outputType);
        }

        return outputType;
    }

    @SuppressWarnings("UnstableApiUsage")
    private GraphQLInputType resolveInputType(Parameter parameter) {
        boolean isNotNull = parameter.isAnnotationPresent(NotNull.class);
        TypeToken<?> typeToken = TypeToken.of(parameter.getParameterizedType());

        GraphQLInputType inputType = resolveGraphQLType(typeToken, cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> (GraphQLInputType) ele)
                .orElse(GraphQLTypeReference.typeRef(resolveTypeName(cls)))
        );

        if (isNotNull) {
            inputType = GraphQLNonNull.nonNull(inputType);
        }

        return inputType;
    }


    /**
     * 解析方法参数
     *
     * @param method
     * @return
     */
    private List<GraphQLArgument> resolveFieldArguments(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLArgument.class))
                .flatMap(this::resolveFieldArgument)
                .collect(Collectors.toList());
    }

    /**
     * 解析参数
     *
     * @param parameter
     * @return
     */
    private Stream<GraphQLArgument> resolveFieldArgument(Parameter parameter) {
        GraphQLInputType inputType = resolveInputType(parameter);

        GraphQLArgument argument = GraphQLArgument.newArgument()
                .name(resolveArgumentName(parameter))
                .description(resolveArgumentDescription(parameter))
                .type(inputType)
                .defaultValue(resolveArgumentDefaultValue(parameter, GraphQLTypeUtil.unwrapNonNull(inputType)))
                .build();

        return Stream.of(argument);
    }

    /**
     * 解析 GraphQL 类型名称
     *
     * @param cls 需要解析的类型
     * @return 类型名称
     */
    private static String resolveTypeName(Class<?> cls) {
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
    private static String resolveFieldName(Method method, Field field) {
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
    private static String resolveArgumentName(Parameter parameter) {
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
    private static String resolveTypeDescription(Class<?> cls) {
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
    private static String resolveFieldDescription(Method method, Field field) {
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
    private static String resolveArgumentDescription(Parameter parameter) {
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
    private static String resolveFieldDeprecate(Method method, Field field) {
        return Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLDeprecate.class))
                .map(GraphQLDeprecate::value)
                .orElseGet(() ->
                        Optional.ofNullable(method.getAnnotation(GraphQLDeprecate.class))
                                .map(GraphQLDeprecate::value)
                                .orElse(null)
                );
    }


    private static Object resolveArgumentDefaultValue(Parameter parameter, GraphQLType type) {
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
                                .map(cls -> {
                                    try {
                                        return cls.newInstance().get();
                                    } catch (InstantiationException | IllegalAccessException e) {
                                        //TODO
                                        throw new RuntimeException(e);
                                    }
                                })
                                .orElse(null)
                );
    }


    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    private static <T extends GraphQLType> T resolveGraphQLType(TypeToken<?> typeToken, Function<Class<?>, ? extends GraphQLType> typeMapper) {
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
            return (T) GraphQLList.list(result);
        }

        return (T) result;
    }


    private static String simplifyName(String name) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, GETTER_PREFIX.matcher(name).replaceFirst("$2"));
    }

}
