package com.ltsoft.graphql.resolver;

import com.google.common.base.Splitter;
import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.TypeResolver;
import com.ltsoft.graphql.annotations.GraphQLComment;
import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.impl.FieldDefinitionCollector;
import com.ltsoft.graphql.provider.ExtensionTypeProvider;
import graphql.language.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;

public abstract class BasicTypeResolver<T extends Definition> implements TypeResolver<T> {

    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults();

    protected abstract TypeProvider<T> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver);

    @Override
    public TypeProvider<T> resolve(Type javaType, Function<Type, TypeProvider<?>> resolver) {
        checkArgument(isSupport(javaType));

        //noinspection UnstableApiUsage
        Class<?> cls = TypeToken.of(javaType).getRawType();

        TypeProvider<T> provider = resolve(cls, resolver);

        if (isGraphQLExtensionType(cls)) {
            //封装为 Extension 类型
            Definition<T> definition = wrapAsExtension(provider.getDefinition());
            //触发扩展类型的解析，注册扩展类型到解析队列
            TypeProvider<?> parentProvider = resolver.apply(cls.getAnnotation(GraphQLTypeExtension.class).value());

            return new ExtensionTypeProvider<>(definition, provider, parentProvider);
        }

        return provider;
    }

    @SuppressWarnings({"UnstableApiUsage", "WeakerAccess"})
    protected List<Comment> getComment(Class<?> cls) {
        return Optional.ofNullable(cls.getAnnotation(GraphQLComment.class))
                .map(GraphQLComment::value)
                .map(LINE_SPLITTER::splitToList)
                .map(list -> list.stream().map(ele -> new Comment(ele, getSourceLocation(cls))).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * 解析 GraphQL 类型描述
     *
     * @param cls 需要解析的类型
     * @return 类型描述
     */
    @SuppressWarnings("WeakerAccess")
    protected Description getDescription(Class<?> cls) {
        return resolveDescription(cls)
                .map(str -> new Description(str, getSourceLocation(cls), str.contains("\n")))
                .orElse(null);
    }

    /**
     * 模拟 SourceLocation，仅记录类名
     *
     * @param cls 需要解析的类
     * @return SourceLocation 信息
     */
    @SuppressWarnings("WeakerAccess")
    protected SourceLocation getSourceLocation(Class<?> cls) {
        return new SourceLocation(0, 0, cls.getName());
    }

    @SuppressWarnings("WeakerAccess")
    protected List<Directive> getDirective(Class<?> cls, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        return ResolveUtil.resolveDirective(cls.getAnnotations(), resolver);
    }

    @SuppressWarnings("WeakerAccess")
    protected List<FieldDefinition> getFieldDefinitions(Class<?> cls, Predicate<FieldInformation> filter, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        return resolveFields(cls)
                .filter(FieldInformation::isNotIgnore)
                .filter(FieldInformation::isGetter)
                .filter(filter)
                .map(info -> info.getFieldDefinition(resolver))
                .collect(new FieldDefinitionCollector<>(FieldDefinition::getName));
    }

    @SuppressWarnings("WeakerAccess")
    protected List<InputValueDefinition> getInputValueDefinitions(Class<?> cls, Predicate<FieldInformation> filter, Function<Type, TypeProvider<?>> resolver) {
        return resolveFields(cls)
                .filter(FieldInformation::isSetter)
                .filter(FieldInformation::isNotIgnore)
                .filter(filter)
                .map(info -> info.getInputValueDefinition(new Class[0], resolver))
                .collect(new FieldDefinitionCollector<>(InputValueDefinition::getName));
    }

    @SuppressWarnings("unchecked")
    private <D extends Definition> D wrapAsExtension(D definition) {
        if (definition instanceof EnumTypeDefinition) {
            return (D) copyProperties(EnumTypeExtensionDefinition.newEnumTypeExtensionDefinition()).apply(definition).build();
        } else if (definition instanceof InputObjectTypeDefinition) {
            return (D) copyProperties(InputObjectTypeExtensionDefinition.newInputObjectTypeExtensionDefinition()).apply(definition).build();
        } else if (definition instanceof InterfaceTypeDefinition) {
            return (D) copyProperties(InterfaceTypeExtensionDefinition.newInterfaceTypeExtensionDefinition()).apply(definition)
                    .definitions(((InterfaceTypeDefinition) definition).getFieldDefinitions())
                    .build();
        } else if (definition instanceof ObjectTypeDefinition) {
            return (D) copyProperties(ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition()).apply(definition).build();
        } else if (definition instanceof UnionTypeDefinition) {
            return (D) copyProperties(UnionTypeExtensionDefinition.newUnionTypeExtensionDefinition()).apply(definition).build();
        }

        throw new IllegalArgumentException(String.format("Unsupported to wrap definition '%s' as extension type", definition.getClass()));
    }

    private <E extends NodeBuilder> Function<Object, E> copyProperties(E builder) {
        return source -> {
            Class<?> sourceType = source.getClass();
            Class<?> builderType = builder.getClass();

            Arrays.stream(builderType.getMethods())
                    .filter(method -> method.getReturnType().equals(builderType))
                    .filter(method -> method.getParameterCount() == 1)
                    .forEach(setter -> {
                        String setterName = setter.getName();
                        String getterName = "get" + setterName.substring(0, 1).toUpperCase() + setterName.substring(1);

                        try {
                            Method getter = sourceType.getMethod(getterName);
                            setter.invoke(builder, getter.invoke(source));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new IllegalArgumentException(String.format("copy property '%s' from '%s' to '%s' fail.", setterName, sourceType, builderType), e);
                        } catch (NoSuchMethodException e) {
                            //do nothing
                        }
                    });

            return builder;
        };
    }
}
