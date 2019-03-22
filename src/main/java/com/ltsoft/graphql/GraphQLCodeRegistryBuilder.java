package com.ltsoft.graphql;

import com.ltsoft.graphql.annotations.*;
import com.ltsoft.graphql.impl.DefaultDataFetcher;
import com.ltsoft.graphql.impl.GraphQLArgumentProvider;
import com.ltsoft.graphql.impl.GraphQLDataFetchingEnvironmentProvider;
import com.ltsoft.graphql.impl.GraphQLEnvironmentProvider;
import com.ltsoft.graphql.visibility.FieldVisibilityFilter;
import com.ltsoft.graphql.visibility.RuntimeFieldVisibilityFilter;
import com.ltsoft.graphql.visibility.TypeVisibilityFilter;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.TypeResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;
import static graphql.schema.FieldCoordinates.coordinates;

public final class GraphQLCodeRegistryBuilder {

    private final Set<Class<?>> types = new HashSet<>();
    private final ServiceInstanceFactory serviceInstanceFactory;

    private List<FieldVisibilityFilter> fieldFilters = new ArrayList<>();
    private List<TypeVisibilityFilter> typeFilters = new ArrayList<>();
    private List<ArgumentProviderFactory<?>> argumentFactories = new ArrayList<>();

    public GraphQLCodeRegistryBuilder(ServiceInstanceFactory serviceInstanceFactory) {
        this.serviceInstanceFactory = serviceInstanceFactory;
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
        checkNotNull(serviceInstanceFactory);

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
            TypeResolver typeResolver = serviceInstanceFactory.provide(cls.getAnnotation(GraphQLInterface.class).typeResolver());
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
            TypeResolver typeResolver = serviceInstanceFactory.provide(cls.getAnnotation(GraphQLUnion.class).typeResolver());
            builder.typeResolver(resolveTypeName(cls), typeResolver);
        }
    }

    private void loadDataFetcher(Class<?> cls, GraphQLCodeRegistry.Builder builder) {
        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(GraphQLDataFetcher.class)) {
                Object serviceInstance = serviceInstanceFactory.provide(cls);
                List<ArgumentProvider<?>> factories = resolveArgumentFactories(method);
                DefaultDataFetcher dataFetcher = new DefaultDataFetcher(method, serviceInstance, factories);

                builder.dataFetcher(coordinates(resolveTypeName(cls), dataFetcher.getFieldName()), dataFetcher);
            }
        }
    }

    private List<ArgumentProvider<?>> resolveArgumentFactories(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> {
                    ArgumentProvider<?> provider = argumentFactories.stream()
                            .filter(ele -> ele.isSupport(parameter, method))
                            .map(ele -> ele.apply(parameter, method))
                            .findFirst()
                            .orElse(null);

                    return provider != null ? provider : buildDefaultArgumentProvider(parameter, method);
                }).collect(Collectors.toList());
    }

    private static ArgumentProvider<?> buildDefaultArgumentProvider(Parameter parameter, Method method) {
        if (parameter.isAnnotationPresent(GraphQLArgument.class)) {
            return new GraphQLArgumentProvider(parameter);
        } else if (parameter.isAnnotationPresent(GraphQLEnvironment.class)) {
            return new GraphQLEnvironmentProvider(parameter.getAnnotation(GraphQLEnvironment.class));
        } else if (parameter.getType().equals(DataFetchingEnvironment.class)) {
            return new GraphQLDataFetchingEnvironmentProvider();
        } else {
            throw new IllegalArgumentException(String.format(
                    "Can not resolve parameter '%s#%s.%s' type with '%s'",
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    parameter.getName(),
                    parameter.getType().getSimpleName())
            );
        }
    }
}
