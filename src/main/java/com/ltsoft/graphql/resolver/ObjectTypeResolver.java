package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.*;
import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.GraphQLArgumentProvider;
import com.ltsoft.graphql.impl.GraphQLEnvironmentProvider;
import com.ltsoft.graphql.impl.ServiceDataFetcher;
import graphql.language.Definition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.ltsoft.graphql.resolver.ResolveUtil.hasGraphQLAnnotation;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class ObjectTypeResolver extends BasicTypeResolver<ObjectTypeDefinition> {

    private static Logger LOGGER = LoggerFactory.getLogger(ObjectTypeResolver.class);

    private InstanceFactory instanceFactory;
    private List<ArgumentProviderFactory<?>> argumentFactories;

    ObjectTypeResolver(InstanceFactory instanceFactory, List<ArgumentProviderFactory<?>> argumentFactories) {
        this.instanceFactory = instanceFactory;
        this.argumentFactories = argumentFactories;
    }

    @Override
    public boolean isSupport(Type javaType) {
        //noinspection UnstableApiUsage
        Class<?> cls = TypeToken.of(javaType).getRawType();

        return !cls.isEnum() && hasGraphQLAnnotation(cls, GraphQLType.class);
    }

    @Override
    TypeProvider<ObjectTypeDefinition> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        ObjectTypeDefinition definition = ObjectTypeDefinition.newObjectTypeDefinition()
                .comments(getComment(cls))
                .description(getDescription(cls))
                .directives(getDirective(cls, resolver))
                .fieldDefinitions(getFieldDefinitions(cls, this::isGraphQLField, resolver))
                .implementz(resolveInterfaces(cls, resolver))
                .name(resolveTypeName(cls))
                .sourceLocation(getSourceLocation(cls))
                .build();

        return new TypeProvider<ObjectTypeDefinition>() {
            @Override
            public Definition<ObjectTypeDefinition> getDefinition() {
                return definition;
            }

            @Override
            public UnaryOperator<RuntimeWiring.Builder> getWiringOperator() {
                return builder -> {
                    loadDataFetcher(cls, builder);
                    loadDefaultDataFetcher(cls, builder);

                    return builder;
                };
            }
        };
    }

    private List<graphql.language.Type> resolveInterfaces(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        //noinspection UnstableApiUsage
        return TypeToken.of(cls).getTypes().stream()
                .map(TypeToken::getRawType)
                .filter(ele -> ele.isAnnotationPresent(GraphQLInterface.class))
                .map(resolver::apply)
                .map(TypeProvider::getTypeName)
                .map(TypeName::new)
                .collect(Collectors.toList());
    }

    /**
     * 过滤字段信息。
     * 字段可从声明了 {@link GraphQLType}、{@link GraphQLInterface} 或 {@link GraphQLSupperClass} 的父类继承。
     *
     * @param info 字段信息
     * @return 是否有效
     */
    private boolean isGraphQLField(FieldInformation info) {
        Class<?> declaringClass = info.getDeclaringClass();

        return info.isNotIgnore() && (hasGraphQLAnnotation(declaringClass, GraphQLType.class)
                || hasGraphQLAnnotation(declaringClass, GraphQLInterface.class)
                || declaringClass.isAnnotationPresent(GraphQLSupperClass.class)
        );
    }

    private void loadDataFetcher(Class<?> cls, RuntimeWiring.Builder wiringBuilder) {
        String parentType = resolveTypeName(cls);

        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(GraphQLDataFetcher.class)) {
                List<ArgumentProvider<?>> factories = resolveArgumentFactories(cls, method);
                ServiceDataFetcher dataFetcher = new ServiceDataFetcher(instanceFactory.provide(cls), method, factories);

                wiringBuilder.type(parentType, builder -> builder.dataFetcher(dataFetcher.getFieldName(), dataFetcher));

                LOGGER.info("Bind GraphQL Field {}.{} to Java method {}#{}", parentType, dataFetcher.getFieldName(), cls.getName(), method.getName());
            }
        }
    }

    private void loadDefaultDataFetcher(Class<?> cls, RuntimeWiring.Builder wiringBuilder) {
        if (cls.isAnnotationPresent(GraphQLDefaultDataFetcher.class)) {
            String typeName = resolveTypeName(cls);
            DefaultDataFetcherFactory dataFetcherFactory = instanceFactory.provide(cls.getAnnotation(GraphQLDefaultDataFetcher.class).value());

            wiringBuilder.type(typeName, builder -> builder.defaultDataFetcher(dataFetcherFactory.get(typeName)));
        }
    }

    private List<ArgumentProvider<?>> resolveArgumentFactories(Class<?> cls, Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> {
                    ArgumentProvider<?> provider = argumentFactories.stream()
                            .filter(ele -> ele.isSupport(cls, method, parameter))
                            .findFirst()
                            .map(ele -> ele.build(cls, method, parameter))
                            .orElse(null);
                    //这里直接使用 orElse 会触发编译错误
                    return provider != null ? provider : buildDefaultArgumentProvider(cls, method, parameter);
                }).collect(Collectors.toList());
    }

    private ArgumentProvider<?> buildDefaultArgumentProvider(Class<?> cls, Method method, Parameter parameter) {
        if (parameter.isAnnotationPresent(GraphQLArgument.class)) {
            return new GraphQLArgumentProvider(cls, method, parameter, instanceFactory);
        } else if (parameter.isAnnotationPresent(GraphQLEnvironment.class)) {
            return new GraphQLEnvironmentProvider(parameter.getAnnotation(GraphQLEnvironment.class));
        } else if (parameter.getType().equals(DataFetchingEnvironment.class)) {
            return environment -> environment;
        } else {
            return environment -> null;
        }
    }
}
