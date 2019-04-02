package com.ltsoft.graphql;

import com.google.common.reflect.ClassPath;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import com.ltsoft.graphql.resolver.ResolveUtil;
import com.ltsoft.graphql.visibility.FieldVisibilityFilter;
import com.ltsoft.graphql.visibility.RuntimeFieldVisibilityFilter;
import com.ltsoft.graphql.visibility.TypeVisibilityFilter;
import graphql.language.Document;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("WeakerAccess")
public final class GraphQLSchemaBuilder {

    private final Map<GraphQLScalarType, Class<?>> scalarTypeMap = new HashMap<>();
    private final Set<String> packageNames = new HashSet<>();
    private final Set<Class<?>> types = new HashSet<>();
    private final List<ArgumentProviderFactory<?>> argumentProviderFactories = new ArrayList<>();
    private final List<FieldVisibilityFilter> fieldFilters = new ArrayList<>();
    private final List<TypeVisibilityFilter> typeFilters = new ArrayList<>();
    private final List<TypeDefinitionRegistry> typeDefinitionRegistries = new ArrayList<>();
    private final List<TypeResolver<?>> typeResolvers = new ArrayList<>();
    private final List<UnaryOperator<Document.Builder>> documentProcessors = new ArrayList<>();
    private final List<UnaryOperator<RuntimeWiring.Builder>> runtimeWiringProcessors = new ArrayList<>();

    private SchemaGenerator.Options options = SchemaGenerator.Options.defaultOptions();
    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    public GraphQLSchemaBuilder addScalar(GraphQLScalarType scalarType, Class<?> javaType) {
        scalarTypeMap.put(scalarType, javaType);
        return this;
    }

    public GraphQLSchemaBuilder addPackage(String... packages) {
        Collections.addAll(packageNames, packages);
        return this;
    }

    public GraphQLSchemaBuilder addType(Class<?>... classes) {
        Collections.addAll(types, classes);
        return this;
    }

    public GraphQLSchemaBuilder argumentFactory(ArgumentProviderFactory<?>... factories) {
        Collections.addAll(argumentProviderFactories, factories);
        return this;
    }

    public GraphQLSchemaBuilder fieldFilter(FieldVisibilityFilter... filters) {
        Collections.addAll(fieldFilters, filters);
        return this;
    }

    public GraphQLSchemaBuilder typeFilter(TypeVisibilityFilter... filters) {
        Collections.addAll(typeFilters, filters);
        return this;
    }

    public GraphQLSchemaBuilder typeResolver(TypeResolver<?>... resolvers) {
        Collections.addAll(typeResolvers, resolvers);
        return this;
    }

    public GraphQLSchemaBuilder typeDefinitionRegistry(TypeDefinitionRegistry... registries) {
        Collections.addAll(typeDefinitionRegistries, registries);
        return this;
    }

    @SafeVarargs
    public final GraphQLSchemaBuilder document(UnaryOperator<Document.Builder>... builderUnaryOperators) {
        Collections.addAll(documentProcessors, builderUnaryOperators);
        return this;
    }

    @SafeVarargs
    public final GraphQLSchemaBuilder runtimeWiring(UnaryOperator<RuntimeWiring.Builder>... builderUnaryOperators) {
        Collections.addAll(runtimeWiringProcessors, builderUnaryOperators);
        return this;
    }

    public GraphQLSchemaBuilder instanceFactory(InstanceFactory instanceFactory) {
        this.instanceFactory = requireNonNull(instanceFactory);
        return this;
    }

    public GraphQLSchemaBuilder options(SchemaGenerator.Options options) {
        this.options = options;
        return this;
    }

    public GraphQLSchema build() {
        GraphQLDocumentBuilder documentBuilder = new GraphQLDocumentBuilder();
        GraphQLRuntimeWiringBuilder runtimeWiringBuilder = new GraphQLRuntimeWiringBuilder();

        Stream.concat(types.stream(), packageNames.stream().flatMap(this::searchPackage))
                .distinct()
                .forEach(cls -> {
                    documentBuilder.withType(cls);
                    runtimeWiringBuilder.withType(cls);
                });

        scalarTypeMap.forEach(documentBuilder::addScalar);
        typeResolvers.forEach(documentBuilder::withTypeResolver);

        documentBuilder.getAllExtensionScalars().forEach(runtimeWiringBuilder::withScalar);

        Document document = combineUnaryOperator(documentProcessors).apply(documentBuilder.builder()).build();
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().buildRegistry(document);

        for (TypeDefinitionRegistry registry : typeDefinitionRegistries) {
            typeDefinitionRegistry = typeDefinitionRegistry.merge(registry);
        }

        runtimeWiringBuilder
                .setArgumentFactories(argumentProviderFactories)
                .setInstanceFactory(instanceFactory);

        RuntimeWiring runtimeWiring = combineUnaryOperator(runtimeWiringProcessors).apply(
                runtimeWiringBuilder.builder().fieldVisibility(new RuntimeFieldVisibilityFilter(typeFilters, fieldFilters))
        ).build();

        return new SchemaGenerator().makeExecutableSchema(options, typeDefinitionRegistry, runtimeWiring);
    }

    private <T> UnaryOperator<T> combineUnaryOperator(Collection<UnaryOperator<T>> unaryOperators) {
        return unaryOperators.stream()
                .reduce((left, right) -> builder -> right.apply(left.apply(builder)))
                .orElse(UnaryOperator.identity());
    }

    private Stream<? extends Class<?>> searchPackage(String packageName) {
        try {
            //noinspection UnstableApiUsage
            return ClassPath.from(getClass().getClassLoader()).getTopLevelClasses(packageName).stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(ResolveUtil::canResolve);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Search package '%s' but fail", packageName), e);
        }
    }
}
