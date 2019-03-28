package com.ltsoft.graphql;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.GraphQLArgumentProvider;
import com.ltsoft.graphql.impl.GraphQLEnvironmentProvider;
import com.ltsoft.graphql.impl.ServiceDataFetcher;
import com.ltsoft.graphql.resolver.EnumFieldValueProvider;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ltsoft.graphql.resolver.ResolveUtil.hasGraphQLAnnotation;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public final class GraphQLRuntimeWiringBuilder {

    private static Logger LOGGER = LoggerFactory.getLogger(GraphQLRuntimeWiringBuilder.class);

    private final Set<Class<?>> types = new HashSet<>();
    private final Set<GraphQLScalarType> scalarTypeSet = new HashSet<>();
    private final Map<String, EnumFieldValueProvider> enumValueProviders = new HashMap<>();
    private List<ArgumentProviderFactory<?>> argumentFactories = new ArrayList<>();

    private InstanceFactory instanceFactory;

    public GraphQLRuntimeWiringBuilder withScalar(GraphQLScalarType... scalarTypes) {
        Collections.addAll(scalarTypeSet, scalarTypes);
        return this;
    }

    public GraphQLRuntimeWiringBuilder withType(Class<?>... classes) {
        Collections.addAll(types, classes);
        return this;
    }

    public GraphQLRuntimeWiringBuilder setArgumentFactories(List<ArgumentProviderFactory<?>> factories) {
        this.argumentFactories = checkNotNull(factories);
        return this;
    }

    public GraphQLRuntimeWiringBuilder setInstanceFactory(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
        return this;
    }

    public RuntimeWiring.Builder builder() {
        checkNotNull(instanceFactory);

        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

        scalarTypeSet.forEach(builder::scalar);

        types.forEach(cls -> loadTypeDefinition(cls, builder));

        enumValueProviders.forEach((typeName, provider) -> builder.type(typeName, e -> e.enumValues(provider)));

        return builder;
    }

    private void loadTypeDefinition(Class<?> cls, RuntimeWiring.Builder wiringBuilder) {
        if (hasGraphQLAnnotation(cls, GraphQLType.class)) {
            if (cls.isEnum()) {
                loadEnumValueProvider(cls, wiringBuilder);
            } else {
                loadDataFetcher(cls, wiringBuilder);
            }
        } else if (cls.isAnnotationPresent(GraphQLInterface.class)) {
            TypeResolver typeResolver = instanceFactory.provide(cls.getAnnotation(GraphQLInterface.class).typeResolver());

            wiringBuilder.type(resolveTypeName(cls), builder -> builder.typeResolver(typeResolver));
        } else if (cls.isAnnotationPresent(GraphQLUnion.class)) {
            TypeResolver typeResolver = instanceFactory.provide(cls.getAnnotation(GraphQLUnion.class).typeResolver());

            wiringBuilder.type(resolveTypeName(cls), builder -> builder.typeResolver(typeResolver));
        }

        if (cls.isAnnotationPresent(GraphQLDefaultDataFetcher.class)) {
            loadDefaultDataFetcher(cls, wiringBuilder);
        }
    }

    private void loadEnumValueProvider(Class<?> cls, RuntimeWiring.Builder wiringBuilder) {
        EnumFieldValueProvider provider = new EnumFieldValueProvider(cls);
        EnumFieldValueProvider result = enumValueProviders.compute(provider.getTypeName(), (key, existed) ->
                existed == null ? provider : provider.merge(existed)
        );

        wiringBuilder.type(result.getTypeName(), builder -> builder.enumValues(result));
    }

    private void loadDataFetcher(Class<?> cls, RuntimeWiring.Builder wiringBuilder) {
        String parentType = resolveTypeName(cls);
        Object serviceInstance = instanceFactory.provide(cls);

        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(GraphQLDataFetcher.class)) {
                List<ArgumentProvider<?>> factories = resolveArgumentFactories(cls, method);
                ServiceDataFetcher dataFetcher = new ServiceDataFetcher(serviceInstance, method, factories);

                wiringBuilder.type(parentType, builder -> builder.dataFetcher(dataFetcher.getFieldName(), dataFetcher));

                LOGGER.info("Bind GraphQL Field {}.{} to Java method {}#{}", parentType, dataFetcher.getFieldName(), cls.getName(), method.getName());
            }
        }
    }

    private void loadDefaultDataFetcher(Class<?> cls, RuntimeWiring.Builder wiringBuilder) {
        String typeName = resolveTypeName(cls);
        DefaultDataFetcherFactory dataFetcherFactory = instanceFactory.provide(cls.getAnnotation(GraphQLDefaultDataFetcher.class).value());

        wiringBuilder.type(typeName, builder -> builder.defaultDataFetcher(dataFetcherFactory.get(typeName)));
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
