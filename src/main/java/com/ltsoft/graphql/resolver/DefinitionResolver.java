package com.ltsoft.graphql.resolver;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.ServiceInstanceFactory;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.DefaultServiceInstanceFactory;
import com.ltsoft.graphql.types.ScalarTypeRepository;
import graphql.language.*;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.TypeInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;

@SuppressWarnings("UnstableApiUsage")
public class DefinitionResolver {

    private static Pattern SETTER_PREFIX = Pattern.compile("^set[A-Z]?\\S*");

    private final ServiceInstanceFactory instanceFactory;
    private final ScalarTypeRepository typeRepository = new ScalarTypeRepository();

    public DefinitionResolver() {
        this(new DefaultServiceInstanceFactory());
    }

    public DefinitionResolver(ServiceInstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    public DirectiveDefinition directive(Class<?> cls) {
        checkArgument(cls.isAnnotationPresent(GraphQLDirectiveLocations.class));

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
                        .name(resolveTypeName(targetClass))
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
                        .name(resolveTypeName(targetClass))
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
                    .name(resolveTypeName(targetClass))
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
                    .name(resolveTypeName(targetClass))
                    .sourceLocation(inputObjectTypeDefinition.getSourceLocation())
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
        checkAnnotation(cls, GraphQLType.class);

        List<EnumValueDefinition> definitions = Arrays.stream(cls.getFields())
                .map(field -> EnumValueDefinition.newEnumValueDefinition()
                        .description(ResolveUtil.resolveDescription(null, field))
                        .directives(resolveDirective(null, field))
                        .name(resolveFieldName(null, field))
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
        checkAnnotation(cls, GraphQLInterface.class);

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
        checkAnnotation(cls, GraphQLInput.class);

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
        checkAnnotation(cls, GraphQLType.class);

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
        checkArgument(cls.isAnnotationPresent(GraphQLUnion.class));

        List<Type> possibleTypes = Arrays.stream(cls.getAnnotation(GraphQLUnion.class).possibleTypes())
                .map(ResolveUtil::resolveTypeName)
                .map(TypeName::new)
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
        // field 不支持 ignore
        return resolveClassExtensions(TypeToken.of(cls), filter)
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
    private FieldDefinition resolveField(Method method, Field field) {
        return FieldDefinition.newFieldDefinition()
                .comments(resolveComment(method, field))
                .description(ResolveUtil.resolveDescription(method, field))
                .directives(resolveDirective(method, field))
                .inputValueDefinitions(resolveFieldInputs(method))
                .name(resolveFieldName(method, field))
                .sourceLocation(resolveSourceLocation(method, field))
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
    private Type<Type> resolveFieldType(Method method, Field field) {
        Invokable<?, Object> invokable = Invokable.from(method);

        boolean isNotNull = Optional.ofNullable(field)
                .map(ele -> ele.isAnnotationPresent(GraphQLNotNull.class))
                .orElse(invokable.isAnnotationPresent(GraphQLNotNull.class));

        return wrapGraphQLType(invokable.getReturnType(), cls -> typeRepository.findMappingScalarType(cls)
                .map(ele -> new TypeName(ele.getName()))
                //若无法识别为 ScalarType，则使用类型引用作为 OutputType
                .orElse(new TypeName(resolveTypeName(cls))), isNotNull);
    }

    /**
     * 解析 GraphQL Field Arguments
     *
     * @param method 字段关联的方法
     * @return GraphQL Input Value
     */
    private List<InputValueDefinition> resolveFieldInputs(Method method) {
        Class<?>[] views = Optional.ofNullable(method.getAnnotation(GraphQLView.class))
                .map(GraphQLView::value)
                .orElse(new Class[0]);

        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLArgument.class))
                .flatMap(parameter -> resolveFieldArgument(parameter, views))
                .collect(Collectors.toList());
    }

    private List<InputValueDefinition> resolveDirectiveArguments(Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .map(method -> InputValueDefinition.newInputValueDefinition()
                        .comments(resolveComment(method, null))
                        .defaultValue(resolveInputDefaultValue(method.getAnnotation(GraphQLDefaultValue.class), Invokable.from(method).getReturnType()))
                        .name(method.getName())
                        .sourceLocation(resolveSourceLocation(method, null))
                        .type(resolveFieldType(method, null))
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * 解析 GraphQL Argument
     *
     * @param parameter 关联的参数
     * @param views     当前 GraphQLView 信息
     * @return GraphQL Argument
     */
    private Stream<InputValueDefinition> resolveFieldArgument(Parameter parameter, Class<?>[] views) {
        Type inputType = resolveInputTypeDefinition(TypeToken.of(parameter.getParameterizedType()), isNotNull(parameter.getAnnotation(GraphQLNotNull.class), views));

        if (inputType != null) {
            InputValueDefinition argument = InputValueDefinition.newInputValueDefinition()
                    .description(ResolveUtil.resolveDescription(parameter))
                    .defaultValue(resolveArgumentDefaultValue(parameter))
                    .directives(resolveDirective(parameter))
                    .name(resolveArgumentName(parameter))
                    .type(inputType)
                    .build();

            return Stream.of(argument);
        } else {
            return resolveArgumentGroup(parameter, views);
        }
    }

    /**
     * 将输入参数无法识别为 GraphQL Input 的 Java 对象的 Field 参数进行拆解，转换为一组 Input Value
     *
     * @param parameter 关联的参数
     * @param views     当前 GraphQLView 信息
     * @return GraphQL Input Value 定义
     */
    private Stream<InputValueDefinition> resolveArgumentGroup(Parameter parameter, Class<?>[] views) {
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
     * @return GraphQL Input Value 定义
     */
    private Stream<InputValueDefinition> resolveFieldAsArgument(Method method, Field field, Class<?>[] views) {
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

            InputValueDefinition inputType = InputValueDefinition.newInputValueDefinition()
                    .comments(resolveComment(method, field))
                    .defaultValue(resolveFieldInputDefaultValue(method, field))
                    .description(ResolveUtil.resolveDescription(method, field))
                    .directives(resolveDirective(method, field))
                    .name(resolveFieldName(method, field))
                    .sourceLocation(resolveSourceLocation(method, field))
                    .type(resolveInputTypeDefinition(paramType, isNotNull(notNull, views)))
                    .build();

            return Stream.of(inputType);
        }

        return Stream.of();
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
     * @return 参数默认值
     */
    private Value resolveArgumentDefaultValue(Parameter parameter) {
        return resolveInputDefaultValue(parameter.getAnnotation(GraphQLDefaultValue.class), TypeToken.of(parameter.getParameterizedType()));
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
     * 解析 GraphQL 输入参数
     *
     * @param method 关联的方法
     * @param field  同名字段
     * @return Input Value 定义
     */
    private InputValueDefinition resolveInputField(Method method, Field field) {
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
                .description(ResolveUtil.resolveDescription(method, field))
                .directives(resolveDirective(method, field))
                .name(resolveFieldName(method, field))
                .sourceLocation(resolveSourceLocation(method, field))
                .type(inputType)
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
                        return new TypeName(resolveTypeName(cls));
                    }

                    return null;
                }), isNotNull);
    }

    private List<Argument> resolveDirectiveArguments(String[] arguments, Class<?> type) {
        List<InputValueDefinition> definitions = resolveDirectiveArguments(type);

        return Arrays.stream(arguments)
                .map(ele -> Arrays.stream(ele.split(":")).map(String::trim).toArray(String[]::new))
                .filter(items -> items.length == 2)
                .map(items -> {
                    String key = items[0];
                    String val = items[1];

                    InputValueDefinition definition = definitions.stream()
                            .filter(arg -> arg.getName().equals(key))
                            .findFirst()
                            .orElse(null);

                    if (definition != null) {
                        graphql.schema.GraphQLType graphQLType = buildTypeFromAST(definition.getType(), typeName ->
                                typeRepository.getScalarType(typeName)
                                        .orElse(getStandardScalarType(typeName).orElse(null)));

                        if (graphQLType != null) {
                            return new Argument(key, AstValueHelper.astFromValue(val, graphQLType));
                        }
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Stream<Directive> resolveDirective(GraphQLDirective[] annotationsByType) {
        return Arrays.stream(annotationsByType)
                .map(ele -> new Directive(resolveTypeName(ele.type()), resolveDirectiveArguments(ele.arguments(), ele.type())));
    }

    private List<Directive> resolveDirective(Class<?> cls) {
        return resolveDirective(cls.getAnnotationsByType(GraphQLDirective.class))
                .collect(Collectors.toList());
    }

    private List<Directive> resolveDirective(Method method, Field field) {
        Stream<Directive> deprecate = resolveDeprecate(method, field);

        Stream<Directive> fieldDirectives = Optional.ofNullable(field)
                .map(ele -> ele.getAnnotationsByType(GraphQLDirective.class))
                .map(this::resolveDirective)
                .orElse(Stream.of());

        Stream<Directive> methodDirectives = Optional.ofNullable(method)
                .map(ele -> ele.getAnnotationsByType(GraphQLDirective.class))
                .map(this::resolveDirective)
                .orElse(Stream.of());

        return Stream.concat(deprecate, Stream.concat(fieldDirectives, methodDirectives)).collect(Collectors.toList());
    }

    private List<Directive> resolveDirective(Parameter parameter) {
        Stream<Directive> deprecate = resolveDeprecate(parameter);

        Stream<Directive> paramDirectives = resolveDirective(parameter.getAnnotationsByType(GraphQLDirective.class));

        return Stream.concat(deprecate, paramDirectives).collect(Collectors.toList());
    }

    /**
     * 解析 Object 所有的扩展类型
     *
     * @param typeToken Object 类型信息
     * @return 所有扩展类型信息
     */
    private Stream<Class<?>> resolveClassExtensions(TypeToken<?> typeToken, Predicate<Class<?>> filter) {
        Stream<? extends Class<?>> withParents = typeToken.getTypes().stream()
                .map(TypeToken::getRawType)
                .filter(filter);

        Class<?>[] fieldExtensions = Optional.ofNullable(typeToken.getRawType().getAnnotation(GraphQLFieldExtension.class))
                .map(GraphQLFieldExtension::value)
                .orElse(new Class[0]);

        return Stream.concat(withParents, Arrays.stream(fieldExtensions));
    }
}
