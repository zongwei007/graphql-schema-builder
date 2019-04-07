package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.FieldDefinitionCollector;
import graphql.language.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public boolean isField() {
        Class<?> parentType = null;
        if (method != null) {
            parentType = method.getDeclaringClass();
        } else if (field != null) {
            parentType = field.getDeclaringClass();
        }

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

    public boolean isSetter() {
        return method != null && SETTER_PREFIX.matcher(method.getName()).matches();
    }

    private Boolean isNotNull() {
        return getAnnotation(GraphQLNotNull.class) != null;
    }

    public boolean isNotNull(Class<?>[] views) {
        GraphQLNotNull notNull = getAnnotation(GraphQLNotNull.class);

        return Optional.ofNullable(notNull)
                .filter(ele -> ele.view().length == 0 || Arrays.stream(views).anyMatch(view -> Arrays.stream(ele.view()).anyMatch(view::isAssignableFrom)))
                .isPresent();
    }

    public boolean isNotIgnore() {
        return isNotIgnore(new Class[0]);
    }

    public boolean isNotIgnore(Class<?>[] views) {
        GraphQLIgnore ignore = getAnnotation(GraphQLIgnore.class);

        return !Optional.ofNullable(ignore)
                .filter(ele -> ele.view().length == 0 || Arrays.stream(views).anyMatch(view -> Arrays.stream(ele.view()).anyMatch(view::isAssignableFrom)))
                .isPresent();
    }

    public List<Directive> getDirectives(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
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

    //TODO
    public String getName() {
        return resolveFieldName(type, method, field);
    }

    public SourceLocation getSourceLocation() {
        return resolveSourceLocation(method, field);
    }

    private Type getFieldType(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        Type fieldType = resolveGraphQLType(resolveGenericType(type, getGenericType()), resolver, isNotNull());

        //若声明了 TypeReference，则使用 TypeReference 类型进行替换
        GraphQLTypeReference typeReference = getAnnotation(GraphQLTypeReference.class);
        if (typeReference != null) {
            fieldType = replaceTypeName(fieldType, resolveTypeReference(typeReference, resolver));
        }

        return fieldType;
    }

    public FieldDefinition getFieldDefinition(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
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

    /**
     * 解析字段的输入参数定义，仅用于 Directive。
     *
     * @param resolver 扩展类型解析器
     * @return InputValueDefinition
     */
    public InputValueDefinition getInputValueDefinition(Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
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
    public InputValueDefinition getInputValueDefinition(Class<?>[] views, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
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

    private List<Comment> getComments() {
        return resolveComment(method, field);
    }

    //TODO
    Description getDescription() {
        return resolveDescription(type, method, field);
    }

    public boolean test(BiPredicate<Method, Field> predicate) {
        return predicate.test(method, field);
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return test((method, field) -> {
            if (field != null) {
                return field.isAnnotationPresent(annotationClass);
            }

            if (method != null) {
                return method.isAnnotationPresent(annotationClass);
            }

            return false;
        });
    }

    private <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return map((method, field) -> {
            if (field != null) {
                return field.getAnnotation(annotationClass);
            }

            if (method != null) {
                return method.getAnnotation(annotationClass);
            }

            return null;
        });
    }

    public <T> T map(BiFunction<Method, Field, T> mapper) {
        return mapper.apply(method, field);
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

    private static Field findMappingField(Method method) {
        return Arrays.stream(method.getDeclaringClass().getDeclaredFields())
                .filter(ele -> ele.getName().equals(simplifyName(method.getName())))
                .findFirst()
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

    private java.lang.reflect.Type getGenericType() {
        if (method != null) {
            return method.getGenericReturnType();
        }

        if (field != null) {
            return field.getGenericType();
        }

        return null;
    }
}
