package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.scalars.ScalarTypeRepository;
import graphql.language.*;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.TypeInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public final class DefinitionResolver {

    private static Pattern SETTER_PREFIX = Pattern.compile("^set[A-Z]?\\S*");

    private final ScalarTypeRepository typeRepository = new ScalarTypeRepository();

    public DirectiveDefinition directive(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(GraphQLDirective.class));

        return DirectiveDefinition.newDirectiveDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directiveLocations(resolveDirectiveLocation(cls))
                .inputValueDefinitions(resolveDirectiveArguments(cls))
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();
    }

    @SuppressWarnings("unchecked")
    public <T extends TypeDefinition> T extension(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(GraphQLTypeExtension.class));

        Class<?> targetClass = cls.getAnnotation(GraphQLTypeExtension.class).value();

        if (targetClass.isAnnotationPresent(GraphQLType.class)) {
            if (targetClass.isEnum()) {
                EnumTypeDefinition enumTypeDefinition = enumeration(cls);

                return (T) EnumTypeExtensionDefinition.newEnumTypeExtensionDefinition()
                        .comments(enumTypeDefinition.getComments())
                        .description(enumTypeDefinition.getDescription())
                        .directives(enumTypeDefinition.getDirectives())
                        .enumValueDefinitions(enumTypeDefinition.getEnumValueDefinitions())
                        .ignoredChars(enumTypeDefinition.getIgnoredChars())
                        .name(enumTypeDefinition.getName())
                        .sourceLocation(enumTypeDefinition.getSourceLocation())
                        .build();
            } else {
                ObjectTypeDefinition objectTypeDefinition = object(cls);

                return (T) ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition()
                        .comments(objectTypeDefinition.getComments())
                        .description(objectTypeDefinition.getDescription())
                        .directives(objectTypeDefinition.getDirectives())
                        .fieldDefinitions(objectTypeDefinition.getFieldDefinitions())
                        .ignoredChars(objectTypeDefinition.getIgnoredChars())
                        .implementz(objectTypeDefinition.getImplements())
                        .name(objectTypeDefinition.getName())
                        .sourceLocation(objectTypeDefinition.getSourceLocation())
                        .build();
            }
        }

        if (targetClass.isAnnotationPresent(GraphQLInterface.class)) {
            InterfaceTypeDefinition interfaceTypeDefinition = iface(cls);

            return (T) InterfaceTypeExtensionDefinition.newInterfaceTypeExtensionDefinition()
                    .comments(interfaceTypeDefinition.getComments())
                    .definitions(interfaceTypeDefinition.getFieldDefinitions())
                    .directives(interfaceTypeDefinition.getDirectives())
                    .description(interfaceTypeDefinition.getDescription())
                    .ignoredChars(interfaceTypeDefinition.getIgnoredChars())
                    .name(interfaceTypeDefinition.getName())
                    .sourceLocation(interfaceTypeDefinition.getSourceLocation())
                    .build();
        }

        if (targetClass.isAnnotationPresent(GraphQLInput.class)) {
            InputObjectTypeDefinition inputObjectTypeDefinition = input(cls);

            return (T) InputObjectTypeExtensionDefinition.newInputObjectTypeExtensionDefinition()
                    .comments(inputObjectTypeDefinition.getComments())
                    .description(inputObjectTypeDefinition.getDescription())
                    .directives(inputObjectTypeDefinition.getDirectives())
                    .ignoredChars(inputObjectTypeDefinition.getIgnoredChars())
                    .inputValueDefinitions(inputObjectTypeDefinition.getInputValueDefinitions())
                    .name(inputObjectTypeDefinition.getName())
                    .sourceLocation(inputObjectTypeDefinition.getSourceLocation())
                    .build();
        }

        if (targetClass.isAnnotationPresent(GraphQLUnion.class)) {
            UnionTypeDefinition unionTypeDefinition = union(cls);

            return (T) UnionTypeExtensionDefinition.newUnionTypeExtensionDefinition()
                    .comments(unionTypeDefinition.getComments())
                    .description(unionTypeDefinition.getDescription())
                    .directives(unionTypeDefinition.getDirectives())
                    .ignoredChars(unionTypeDefinition.getIgnoredChars())
                    .memberTypes(unionTypeDefinition.getMemberTypes())
                    .name(unionTypeDefinition.getName())
                    .sourceLocation(unionTypeDefinition.getSourceLocation())
                    .build();
        }

        throw new IllegalArgumentException(String.format("Unsupported to extension class '%s' by '%s'", targetClass.getName(), cls.getName()));
    }

    /**
     * 解析 GraphQL Enum 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Enum
     */
    public EnumTypeDefinition enumeration(Class<?> cls) {
        checkArgument(cls.isEnum());
        checkArgument(hasGraphQLAnnotation(cls, GraphQLType.class));

        List<EnumValueDefinition> definitions = Arrays.stream(cls.getFields())
                .filter(field -> isNotIgnore(null, field))
                .map(field -> EnumValueDefinition.newEnumValueDefinition()
                        .description(resolveDescription(cls, null, field))
                        .directives(resolveDirective(null, field))
                        .name(resolveFieldName(cls, null, field))
                        .build())
                .collect(Collectors.toList());

        return EnumTypeDefinition.newEnumTypeDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directives(resolveDirective(cls))
                .enumValueDefinitions(definitions)
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();
    }

    /**
     * 解析 GraphQL Interface 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Interface
     */
    public InterfaceTypeDefinition iface(Class<?> cls) {
        checkArgument(hasGraphQLAnnotation(cls, GraphQLInterface.class));

        return InterfaceTypeDefinition.newInterfaceTypeDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .definitions(resolveFields(cls, ele -> ele.isAnnotationPresent(GraphQLInterface.class) || ele.isAnnotationPresent(GraphQLTypeExtension.class)))
                .directives(resolveDirective(cls))
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();
    }

    /**
     * 解析 GraphQL Input 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Input
     */
    public InputObjectTypeDefinition input(Class<?> cls) {
        checkArgument(hasGraphQLAnnotation(cls, GraphQLInput.class));

        return InputObjectTypeDefinition.newInputObjectDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directives(resolveDirective(cls))
                .inputValueDefinitions(resolveInputFields(cls))
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();
    }

    /**
     * 解析 GraphQL Object 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Object
     */
    public ObjectTypeDefinition object(Class<?> cls) {
        checkArgument(hasGraphQLAnnotation(cls, GraphQLType.class));

        return ObjectTypeDefinition.newObjectTypeDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directives(resolveDirective(cls))
                .fieldDefinitions(resolveFields(cls, ele -> ele.isAnnotationPresent(GraphQLType.class) || ele.isAnnotationPresent(GraphQLTypeExtension.class)))
                .implementz(resolveInterfaces(cls))
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();
    }

    public ScalarTypeDefinition scalar(GraphQLScalarType scalarType, Class<?> javaType) {
        typeRepository.register(javaType, scalarType);

        return typeRepository.getScalarTypeDefinition(scalarType.getName())
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * 解析 GraphQL Union 类型
     *
     * @param cls 需要解析的类
     * @return GraphQL Union
     */
    public UnionTypeDefinition union(Class<?> cls) {
        checkArgument(hasGraphQLAnnotation(cls, GraphQLUnion.class));

        List<Type> possibleTypes = Arrays.stream(cls.getAnnotation(GraphQLUnion.class).possibleTypes())
                .map(ResolveUtil::resolveType)
                .collect(Collectors.toList());

        return UnionTypeDefinition.newUnionTypeDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directives(resolveDirective(cls))
                .memberTypes(possibleTypes)
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();
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
     * @param cls    需要解析的类
     * @param filter 类型过滤
     * @return 字段描述
     */
    private List<FieldDefinition> resolveFields(Class<?> cls, Predicate<Class<?>> filter) {
        return resolveClassExtensions(TypeToken.of(cls), filter)
                .flatMap(ele -> resolveFieldStream(ele,
                        andBiPredicate((method, field) -> !SETTER_PREFIX.matcher(method.getName()).matches(), ResolveUtil::isNotIgnore),
                        (method, field) -> resolveField(cls, method, field))
                )
                .collect(Collectors.toList());
    }

    /**
     * 解析 GraphQL Field
     *
     * @param resolvingCls 当前解析的类
     * @param method       字段关联的方法
     * @param field        字段同名的属性
     * @return GraphQL Field 定义
     */
    private FieldDefinition resolveField(Class<?> resolvingCls, Method method, Field field) {
        return FieldDefinition.newFieldDefinition()
                .comments(resolveComment(method, field))
                .description(resolveDescription(resolvingCls, method, field))
                .directives(resolveDirective(method, field))
                .inputValueDefinitions(resolveFieldInputs(resolvingCls, method))
                .name(resolveFieldName(resolvingCls, method, field))
                .sourceLocation(resolveSourceLocation(method, field))
                .type(resolveFieldType(resolvingCls, method, field))
                .build();
    }

    /**
     * 解析 GraphQL Field 类型
     *
     * @param resolvingCls 正在解析的类
     * @param method       字段关联的方法
     * @param field        字段同名的属性
     * @return GraphQL Field 类型
     */
    private Type<Type> resolveFieldType(Class<?> resolvingCls, Method method, Field field) {
        boolean isNotNull = Optional.ofNullable(field)
                .map(ele -> ele.isAnnotationPresent(GraphQLNotNull.class))
                .orElse(method.isAnnotationPresent(GraphQLNotNull.class));

        return wrapGraphQLType(resolveGenericType(resolvingCls, method.getGenericReturnType()), cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> new TypeName(ele.getName()))
                //若无法识别为 ScalarType，则使用类型引用作为 OutputType
                .orElse(resolveType(cls)), isNotNull);
    }

    /**
     * 解析 GraphQL Field Arguments
     *
     * @param resolvingCls 当前解析的类型
     * @param method       字段关联的方法
     * @return GraphQL Input Value
     */
    private List<InputValueDefinition> resolveFieldInputs(Class<?> resolvingCls, Method method) {
        Class<?>[] views = Optional.ofNullable(method.getAnnotation(GraphQLView.class))
                .map(GraphQLView::value)
                .orElse(new Class[0]);

        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLArgument.class))
                .flatMap(parameter -> resolveFieldArgument(resolvingCls, parameter, views))
                .collect(Collectors.toList());
    }

    private List<InputValueDefinition> resolveDirectiveArguments(Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .filter(method -> method.getDeclaringClass().equals(cls))
                .filter(method -> isNotIgnore(method, null))
                .map(method -> InputValueDefinition.newInputValueDefinition()
                        .comments(resolveComment(method, null))
                        .defaultValue(resolveInputDefaultValue(method.getAnnotation(GraphQLDefaultValue.class), TypeToken.of(method.getGenericReturnType())))
                        .name(method.getName())
                        .sourceLocation(resolveSourceLocation(method, null))
                        .type(resolveFieldType(cls, method, null))
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * 解析 GraphQL Argument
     *
     * @param resolvingCls 当前解析的类型
     * @param parameter    关联的参数
     * @param views        当前 GraphQLView 信息
     * @return GraphQL Argument
     */
    private Stream<InputValueDefinition> resolveFieldArgument(Class<?> resolvingCls, Parameter parameter, Class<?>[] views) {
        boolean isNotNull = isNotNull(parameter.getAnnotation(GraphQLNotNull.class), views);
        TypeToken<?> typeToken = resolveGenericType(resolvingCls, parameter.getParameterizedType());
        Type inputType = resolveInputTypeDefinition(typeToken, isNotNull);

        if (inputType == null && parameter.isAnnotationPresent(GraphQLMutationType.class)) {
            inputType = resolveType(parameter.getAnnotation(GraphQLMutationType.class).value());

            if (isGraphQLList(typeToken)) {
                inputType = new ListType(inputType);
            }

            if (isNotNull) {
                inputType = new NonNullType(inputType);
            }
        }

        if (inputType != null) {
            InputValueDefinition argument = InputValueDefinition.newInputValueDefinition()
                    .description(resolveDescription(parameter))
                    .defaultValue(resolveArgumentDefaultValue(parameter, typeToken))
                    .directives(resolveDirective(parameter))
                    .name(resolveArgumentName(parameter))
                    .type(inputType)
                    .build();

            return Stream.of(argument);
        } else {
            //将输入参数无法识别为 GraphQL Input 的 Java 对象的 Field 参数进行拆解，转换为一组 Input Value
            return resolveClassExtensions(typeToken, ele -> ele.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class))
                    .flatMap(ele ->
                            resolveFieldStream(ele,
                                    andBiPredicate((method, field) -> SETTER_PREFIX.matcher(method.getName()).matches(), (method, field) -> isNotIgnore(method, field, views)),
                                    (method, field) -> resolveFieldAsArgument(resolvingCls, method, field, views)
                            )
                    );
        }
    }

    /**
     * 将近似于 GraphQL Object 的 Java 对象的方法解析为输入参数
     *
     * @param resolvingCls 当前解析的类型
     * @param method       关联的方法
     * @param field        同名的字段
     * @param views        当前 GraphQLView 信息
     * @return GraphQL Input Value 定义
     */
    private InputValueDefinition resolveFieldAsArgument(Class<?> resolvingCls, Method method, Field field, Class<?>[] views) {
        Parameter parameter = method.getParameters()[0];
        GraphQLNotNull notNull = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLNotNull.class))
                .orElse(parameter.getAnnotation(GraphQLNotNull.class));
        GraphQLMutationType mutationType = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLMutationType.class))
                .orElse(parameter.getAnnotation(GraphQLMutationType.class));

        TypeToken<?> paramType = TypeToken.of(parameter.getParameterizedType());

        if (field != null && paramType.isSupertypeOf(field.getGenericType())) {
            paramType = TypeToken.of(field.getGenericType());
        }

        if (mutationType != null) {
            paramType = TypeToken.of(mutationType.value());
        }

        Type inputType = resolveInputTypeDefinition(paramType, isNotNull(notNull, views));

        return InputValueDefinition.newInputValueDefinition()
                .comments(resolveComment(method, field))
                .defaultValue(resolveFieldInputDefaultValue(method, field))
                .description(resolveDescription(resolvingCls, method, field))
                .directives(resolveDirective(method, field))
                .name(resolveFieldName(resolvingCls, method, field))
                .sourceLocation(resolveSourceLocation(method, field))
                .type(requireNonNull(inputType, String.format("Can not resolve type '%s' as input type", paramType)))
                .build();
    }

    private Value resolveInputDefaultValue(GraphQLDefaultValue defaultValue, TypeToken<?> typeToken) {
        GraphQLScalarType scalarType = Optional.ofNullable(resolveInputTypeDefinition(typeToken, false))
                .map(type -> TypeInfo.typeInfo(type).getName())
                .map(name -> typeRepository.getScalarType(name)
                        .orElse(getStandardScalarType(name).orElse(null)))
                .orElse(null);

        return Optional.ofNullable(defaultValue)
                .map(GraphQLDefaultValue::value)
                .map(str -> {
                    if (scalarType != null) {
                        return AstValueHelper.astFromValue(str, scalarType);
                    }

                    if (typeToken.getRawType().isEnum() && typeToken.getRawType().isAnnotationPresent(GraphQLType.class)) {
                        return new EnumValue(str);
                    }

                    return null;
                })
                .orElse(null);
    }

    /**
     * 解析参数默认值
     *
     * @param parameter 关联参数
     * @param typeToken 参数类型
     * @return 参数默认值
     */
    private Value resolveArgumentDefaultValue(Parameter parameter, TypeToken<?> typeToken) {
        return resolveInputDefaultValue(parameter.getAnnotation(GraphQLDefaultValue.class), typeToken);
    }

    /**
     * 字段映射为参数时，解析参数默认值
     *
     * @param method 关联的方法
     * @param field  同名字段
     * @return 字段默认值
     */
    private Value resolveFieldInputDefaultValue(Method method, Field field) {
        GraphQLDefaultValue defaultValue = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLDefaultValue.class))
                .orElse(method.getAnnotation(GraphQLDefaultValue.class));

        return resolveInputDefaultValue(defaultValue, TypeToken.of(method.getParameters()[0].getParameterizedType()));
    }

    /**
     * 解析 GraphQL Input Object 字段描述
     *
     * @param cls 关联的类
     * @return GraphQL Input Object 字段描述
     */
    private List<InputValueDefinition> resolveInputFields(Class<?> cls) {
        return resolveClassExtensions(TypeToken.of(cls), ele -> ele.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLType.class) || ele.isAnnotationPresent(GraphQLInterface.class) || ele.isAnnotationPresent(GraphQLTypeExtension.class))
                .flatMap(ele -> resolveFieldStream(ele,
                        andBiPredicate((method, field) -> SETTER_PREFIX.matcher(method.getName()).matches(), ResolveUtil::isNotIgnore),
                        (method, field) -> resolveInputField(cls, method, field)
                ))
                .collect(Collectors.toList());
    }

    /**
     * 解析 GraphQL 输入参数
     *
     * @param cls    当前解析类
     * @param method 关联的方法
     * @param field  同名字段
     * @return Input Value 定义
     */
    private InputValueDefinition resolveInputField(Class<?> cls, Method method, Field field) {
        checkArgument(method.getParameterCount() == 1);

        GraphQLNotNull notNull = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLNotNull.class))
                .orElse(method.getAnnotation(GraphQLNotNull.class));

        GraphQLMutationType mutationType = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotation(GraphQLMutationType.class))
                .orElse(method.getAnnotation(GraphQLMutationType.class));

        Parameter parameter = method.getParameters()[0];
        TypeToken<?> paramType = TypeToken.of(parameter.getParameterizedType());

        if (field != null && paramType.isSupertypeOf(field.getGenericType())) {
            paramType = TypeToken.of(field.getGenericType());
        }

        if (mutationType != null) {
            paramType = TypeToken.of(mutationType.value());
        }

        Type inputType = resolveInputTypeDefinition(paramType, isNotNull(notNull, new Class[0]));

        return InputValueDefinition.newInputValueDefinition()
                .comments(resolveComment(method, field))
                .defaultValue(resolveFieldInputDefaultValue(method, field))
                .description(resolveDescription(cls, method, field))
                .directives(resolveDirective(method, field))
                .name(resolveFieldName(cls, method, field))
                .sourceLocation(resolveSourceLocation(method, field))
                .type(requireNonNull(inputType, String.format("Can not resolve type '%s' as input type", paramType)))
                .build();
    }

    /**
     * 解析输入类型
     *
     * @param typeToken 类型信息
     * @param isNotNull 是否非空
     * @return GraphQL 类型
     */
    private Type resolveInputTypeDefinition(TypeToken<?> typeToken, Boolean isNotNull) {
        return wrapGraphQLType(typeToken, cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> new TypeName(ele.getName()))
                .orElseGet(() -> {
                    if (cls.isAnnotationPresent(GraphQLInput.class) || cls.isEnum()) {
                        return resolveType(cls);
                    }

                    return null;
                }), isNotNull);
    }

    /**
     * 将多个 BiPredicate 按 and 合并
     *
     * @param predicates 断言
     * @return 合并后的断言
     */
    @SafeVarargs
    private static <T, U> BiPredicate<T, U> andBiPredicate(BiPredicate<T, U>... predicates) {
        BiPredicate<T, U> predicate = predicates[0];

        for (int i = 1; i < predicates.length; i++) {
            predicate = predicate.and(predicates[i]);
        }

        return predicate;
    }
}
