package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.TypeResolver;
import com.ltsoft.graphql.annotations.GraphQLTypeExtension;
import com.ltsoft.graphql.provider.ExtensionTypeProvider;
import graphql.language.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.ltsoft.graphql.resolver.ResolveUtil.*;

public abstract class BasicTypeResolver<T extends Definition> implements TypeResolver<T> {

    abstract TypeProvider<T> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver);

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

    List<Directive> resolveDirective(Class<?> cls, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        return ResolveUtil.resolveDirective(cls.getAnnotations(), resolver);
    }

    List<FieldDefinition> resolveFieldDefinitions(Class<?> cls, Predicate<FieldInformation> filter, Function<java.lang.reflect.Type, TypeProvider<?>> resolver) {
        return resolveFields(cls)
                .filter(filter)
                .filter(info -> info.test((method, field) -> hasReturnType(method)))
                .map(info -> info.getFieldDefinition(resolver))
                .collect(Collectors.toList());
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
