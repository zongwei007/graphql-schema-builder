package com.ltsoft.graphql;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.GraphQLArgumentProvider;
import com.ltsoft.graphql.impl.GraphQLEnvironmentProvider;
import com.ltsoft.graphql.impl.ServiceDataFetcher;
import com.ltsoft.graphql.visibility.FieldVisibilityFilter;
import com.ltsoft.graphql.visibility.RuntimeFieldVisibilityFilter;
import com.ltsoft.graphql.visibility.TypeVisibilityFilter;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;
import static graphql.schema.FieldCoordinates.coordinates;

public final class GraphQLCodeRegistryBuilder {

    private static Logger LOGGER = LoggerFactory.getLogger(GraphQLCodeRegistryBuilder.class);

    private final Set<Class<?>> types = new HashSet<>();
    private final InstanceFactory instanceFactory;

    private List<FieldVisibilityFilter> fieldFilters = new ArrayList<>();
    private List<TypeVisibilityFilter> typeFilters = new ArrayList<>();
    private List<ArgumentProviderFactory<?>> argumentFactories = new ArrayList<>();

    public GraphQLCodeRegistryBuilder(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    public GraphQLCodeRegistryBuilder withType(Class<?>... classes) {
        Collections.addAll(types, classes);
        return this;
    }

    public GraphQLCodeRegistryBuilder setFieldFilters(List<FieldVisibilityFilter> filters) {
        this.fieldFilters = checkNotNull(filters);
        return this;
    }

    public GraphQLCodeRegistryBuilder setTypeFilters(List<TypeVisibilityFilter> filters) {
        this.typeFilters = checkNotNull(filters);
        return this;
    }

    public GraphQLCodeRegistryBuilder setArgumentFactories(List<ArgumentProviderFactory<?>> factories) {
        this.argumentFactories = checkNotNull(factories);
        return this;
    }

    public GraphQLCodeRegistry.Builder builder() {
        checkNotNull(instanceFactory);

        GraphQLCodeRegistry.Builder builder = GraphQLCodeRegistry.newCodeRegistry();

        builder.fieldVisibility(new RuntimeFieldVisibilityFilter(typeFilters, fieldFilters));

        types.forEach(cls -> loadTypeDefinition(cls, builder));

        return builder;
    }

    private void loadTypeDefinition(Class<?> cls, GraphQLCodeRegistry.Builder builder) {
        if (cls.isAnnotationPresent(GraphQLType.class)) {
            loadDataFetcher(cls, builder);
        }

        if (cls.isAnnotationPresent(GraphQLInterface.class)) {
            TypeResolver typeResolver = instanceFactory.provide(cls.getAnnotation(GraphQLInterface.class).typeResolver());
            builder.typeResolver(resolveTypeName(cls), typeResolver);
        }

        if (cls.isAnnotationPresent(GraphQLTypeExtension.class)) {
            Class<?> targetClass = cls.getAnnotation(GraphQLTypeExtension.class).value();
            //扩展类型仅识别 GraphQLObject 扩展
            if (!targetClass.isEnum() && targetClass.isAnnotationPresent(GraphQLType.class)) {
                loadDataFetcher(cls, builder);
            }
        }

        if (cls.isAnnotationPresent(GraphQLUnion.class)) {
            TypeResolver typeResolver = instanceFactory.provide(cls.getAnnotation(GraphQLUnion.class).typeResolver());
            builder.typeResolver(resolveTypeName(cls), typeResolver);
        }
    }

    private void loadDataFetcher(Class<?> cls, GraphQLCodeRegistry.Builder builder) {
        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(GraphQLDataFetcher.class)) {
                Object serviceInstance = instanceFactory.provide(cls);
                List<ArgumentProvider<?>> factories = resolveArgumentFactories(cls, method);
                ServiceDataFetcher dataFetcher = new ServiceDataFetcher(serviceInstance, method, factories);
                FieldCoordinates coordinates = coordinates(resolveTypeName(cls), dataFetcher.getFieldName());

                builder.dataFetcher(coordinates, dataFetcher);

                LOGGER.info("Bind GraphQL Field {}.{} to Java method {}#{}", coordinates.getTypeName(), coordinates.getFieldName(), cls.getName(), method.getName());
            }
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
