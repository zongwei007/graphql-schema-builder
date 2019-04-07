package com.ltsoft.graphql.resolver;

import com.google.common.base.Splitter;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.FieldDefinitionCollector;
import graphql.language.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;
import static java.util.Objects.requireNonNull;

public class FieldInformation {

    private static Pattern SETTER_PREFIX = Pattern.compile("^set[A-Z]?\\S*");
    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults();

    private final Class<?> type;
    private final Method method;
    private final Field field;

    FieldInformation(Class<?> cls, Method method) {
        this(cls, method, findMappingField(method));
    }

    FieldInformation(Class<?> type, Method method, Field field) {
        checkArgument(type != null);
        checkArgument(method != null || field != null);

        this.type = type;
        this.method = method;
        this.field = field;
    }

    public boolean test(BiPredicate<Method, Field> predicate) {
        return predicate.test(method, field);
    }

    public <T> T map(BiFunction<Method, Field, T> mapper) {
        return mapper.apply(method, field);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @SuppressWarnings("WeakerAccess")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        //判断注解是否允许继承
        boolean isInherited = annotationClass.isAnnotationPresent(Inherited.class);

        if (!isInherited) {
            //非继承注解直接从当前数据读取
            return Optional.ofNullable(method)
                    .map(ele -> ele.getAnnotation(annotationClass))
                    .orElseGet(() ->
                            Optional.ofNullable(field)
                                    .map(ele -> ele.getAnnotation(annotationClass))
                                    .orElse(null)
                    );
        }

        //继承注解优先从同名方法上解析（Java 不支持方法上的注解继承），其次从字段上读取
        return getSomeMethods()
                .filter(ele -> ele.isAnnotationPresent(annotationClass))
                .findFirst()
                .map(ele -> ele.getAnnotation(annotationClass))
                .orElseGet(() ->
                        Optional.ofNullable(field)
                                .map(ele -> ele.getAnnotation(annotationClass))
                                .orElse(null)
                );
    }

    @SuppressWarnings({"WeakerAccess", "UnstableApiUsage"})
    public java.lang.reflect.Type getGenericType() {
        TypeToken<?> typeToken = TypeToken.of(type);

        if (method != null) {
            return typeToken.resolveType(method.getGenericReturnType()).getType();
        } else {
            return typeToken.resolveType(field.getGenericType()).getType();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public Class<?> getDeclaringClass() {
        if (method != null) {
            return method.getDeclaringClass();
        } else {
            return field.getDeclaringClass();
        }
    }

    public Class<?> getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return resolveFieldName(type, method, field);
    }

    public boolean isField() {
        Class<?> parentType = getDeclaringClass();
        Method[] parentMethods = requireNonNull(parentType).getDeclaredMethods();
        Field[] parentFields = parentType.getDeclaredFields();

        if (Arrays.stream(parentMethods).anyMatch(ele -> ele.isAnnotationPresent(GraphQLField.class)) || Arrays.stream(parentFields).anyMatch(ele -> ele.isAnnotationPresent(GraphQLField.class))) {
            if (!isAnnotationPresent(GraphQLField.class)) {
                return false;
            }
        }

        if (parentType.isAnnotationPresent(GraphQLFieldFilter.class)) {
            Class<? extends BiPredicate<Method, Field>> filterType = parentType.getAnnotation(GraphQLFieldFilter.class).value();
            try {
                return test(filterType.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format("Can not construct '%s' as field filter", filterType), e);
            }
        }

        return true;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isSetter() {
        return method != null && SETTER_PREFIX.matcher(method.getName()).matches();
    }

    @SuppressWarnings("WeakerAccess")
    public Boolean isNotNull() {
        return getAnnotation(GraphQLNotNull.class) != null;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isNotNull(Class<?>[] views) {
        GraphQLNotNull notNull = getAnnotation(GraphQLNotNull.class);

        return Optional.ofNullable(notNull)
                .filter(ele -> ele.view().length == 0 || Arrays.stream(views).anyMatch(view -> Arrays.stream(ele.view()).anyMatch(view::isAssignableFrom)))
                .isPresent();
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isNotIgnore() {
        return isNotIgnore(new Class[0]);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isNotIgnore(Class<?>[] views) {
        GraphQLIgnore ignore = getAnnotation(GraphQLIgnore.class);

        return !Optional.ofNullable(ignore)
                .filter(ele -> ele.view().length == 0 || Arrays.stream(views).anyMatch(view -> Arrays.stream(ele.view()).anyMatch(view::isAssignableFrom)))
                .isPresent();
    }

    @SuppressWarnings("WeakerAccess")
    public SourceLocation getSourceLocation() {
        String name;

        if (method != null) {
            name = String.format("%s#%s", method.getDeclaringClass().getName(), method.getName());
        } else {
            name = String.format("%s.%s", field.getDeclaringClass().getName(), field.getName());
        }

        return new SourceLocation(0, 0, name);
    }

    @SuppressWarnings("WeakerAccess")
    public List<Comment> getComments() {
        //noinspection UnstableApiUsage
        return Optional.ofNullable(getAnnotation(GraphQLComment.class))
                .map(GraphQLComment::value)
                .map(value ->
                        LINE_SPLITTER.splitToList(value)
                                .stream()
                                .map(ele -> new Comment(ele, getSourceLocation()))
                                .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());
    }

    public Description getDescription() {
        return Optional.ofNullable(getAnnotation(GraphQLDescription.class))
                .map(GraphQLDescription::value)
                .map(value ->
                        //按 GraphQLFieldDescription 进行格式化
                        Optional.ofNullable(getDeclaringClass())
                                .map(ele -> ele.getAnnotation(GraphQLFieldDescription.class))
                                .map(GraphQLFieldDescription::value)
                                .map(ele -> formatName(ele, value, type))
                                .orElse(value)
                )
                .map(value -> new Description(value, getSourceLocation(), value.contains("\n")))
                .orElse(null);
    }

    FieldDefinition getFieldDefinition(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        return FieldDefinition.newFieldDefinition()
                .comments(getComments())
                .description(getDescription())
                .directives(getDirectives(resolver))
                .inputValueDefinitions(resolveFieldInputs(resolver))
                .name(getName())
                .sourceLocation(getSourceLocation())
                .type(getFieldType(resolver))
                .build();
    }

    List<Directive> getDirectives(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        Stream<Annotation> fieldAnnotations = Optional.ofNullable(field)
                .map(ele -> Arrays.stream(ele.getAnnotations()))
                .orElse(Stream.of());

        Stream<Annotation> methodAnnotations = Optional.ofNullable(method)
                .map(ele -> Arrays.stream(ele.getAnnotations()))
                .orElse(Stream.of());

        return Stream.concat(fieldAnnotations, methodAnnotations)
                .map(ele -> resolveDirective(ele, resolver))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 解析字段的输入参数定义，仅用于 Directive。
     *
     * @param resolver 扩展类型解析器
     * @return InputValueDefinition
     */
    InputValueDefinition getInputValueDefinition(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        Type inputType = getFieldType(resolver);

        return InputValueDefinition.newInputValueDefinition()
                .comments(getComments())
                .defaultValue(resolveInputDefaultValue(getAnnotation(GraphQLDefaultValue.class), inputType, isGraphQLEnumType(type)))
                .description(getDescription())
                .name(getName())
                .sourceLocation(getSourceLocation())
                .type(inputType)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    InputValueDefinition getInputValueDefinition(Class<?>[] views, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        checkArgument(method.getParameterCount() == 1, String.format("Setter of method %s#%s must have only parameter", method.getDeclaringClass().getName(), method.getName()));

        TypeToken<?> paramType = TypeToken.of(method.getParameters()[0].getParameterizedType());

        if (field != null) {
            paramType = TypeToken.of(field.getGenericType());
        }

        Type inputType = resolveGraphQLType(paramType, resolver, isNotNull(views));
        boolean isEnum = isGraphQLEnumType(paramType.getRawType());

        GraphQLMutationType mutationType = getAnnotation(GraphQLMutationType.class);
        GraphQLTypeReference typeReference = getAnnotation(GraphQLTypeReference.class);
        if (mutationType != null) {
            //若声明了 GraphQLMutationType，则优先使用 GraphQLMutationType 类型进行替换
            inputType = replaceTypeName(inputType, resolveMutationType(mutationType, resolver));
        } else if (typeReference != null) {
            //若声明了 TypeReference，则使用 TypeReference 类型进行替换
            inputType = replaceTypeName(inputType, resolveTypeReference(typeReference, resolver));
        }

        return InputValueDefinition.newInputValueDefinition()
                .comments(getComments())
                .defaultValue(resolveInputDefaultValue(getAnnotation(GraphQLDefaultValue.class), inputType, isEnum))
                .description(getDescription())
                .directives(getDirectives(resolver))
                .name(getName())
                .sourceLocation(getSourceLocation())
                .type(requireNonNull(inputType, String.format("Can not resolve type '%s' as input type", paramType)))
                .build();
    }

    private Type getFieldType(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        //noinspection UnstableApiUsage
        Type fieldType = resolveGraphQLType(TypeToken.of(getGenericType()), resolver, isNotNull());

        //若声明了 TypeReference，则使用 TypeReference 类型进行替换
        GraphQLTypeReference typeReference = getAnnotation(GraphQLTypeReference.class);
        if (typeReference != null) {
            fieldType = replaceTypeName(fieldType, resolveTypeReference(typeReference, resolver));
        }

        return fieldType;
    }

    private static Field findMappingField(Method method) {
        return Arrays.stream(method.getDeclaringClass().getDeclaredFields())
                .filter(ele -> ele.getName().equals(simplifyName(method.getName())))
                .findFirst()
                .orElse(null);
    }

    private Stream<Method> getSomeMethods() {
        if (method == null) {
            return Stream.of();
        }

        //noinspection UnstableApiUsage
        return TypeToken.of(type).getTypes()
                .stream()
                .map(TypeToken::getRawType)
                .flatMap(cls ->
                        Arrays.stream(cls.getMethods())
                                .filter(ele -> ele.getName().equals(method.getName()))
                                .filter(ele -> Arrays.equals(ele.getParameterTypes(), method.getParameterTypes()))
                );
    }

    private TypeName resolveMutationType(GraphQLMutationType annotation, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        return Optional.of(annotation.value())
                .map(resolver::apply)
                .map(TypeProvider::getTypeName)
                .map(TypeName::new)
                .orElse(null);
    }

    private List<InputValueDefinition> resolveFieldInputs(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        Class<?>[] views = Optional.ofNullable(method.getAnnotation(GraphQLView.class))
                .map(GraphQLView::value)
                .orElse(new Class[0]);

        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(com.ltsoft.graphql.annotations.GraphQLArgument.class))
                .map(parameter -> new ArgumentInformation(this, parameter, views))
                .flatMap(argument -> argument.getDefinitions(resolver).stream())
                .collect(new FieldDefinitionCollector<>(InputValueDefinition::getName));
    }
}
