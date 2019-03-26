package com.ltsoft.graphql;

import com.google.common.reflect.ClassPath;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import com.ltsoft.graphql.resolver.ResolveUtil;
import com.ltsoft.graphql.visibility.FieldVisibilityFilter;
import com.ltsoft.graphql.visibility.TypeVisibilityFilter;
import graphql.language.Document;
import graphql.schema.GraphQLCodeRegistry;
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

import static com.google.common.base.Preconditions.checkNotNull;

public final class GraphQLSchemaBuilder {

    private final Map<GraphQLScalarType, Class<?>> scalarTypeMap = new HashMap<>();
    private final Set<String> packageNames = new HashSet<>();
    private final Set<Class<?>> types = new HashSet<>();
    private final List<ArgumentProviderFactory<?>> argumentProviderFactories = new ArrayList<>();
    private final List<FieldVisibilityFilter> fieldFilters = new ArrayList<>();
    private final List<TypeVisibilityFilter> typeFilters = new ArrayList<>();
    private final List<TypeDefinitionRegistry> typeDefinitionRegistries = new ArrayList<>();

    private UnaryOperator<GraphQLCodeRegistry.Builder> codeRegistryProcessor = UnaryOperator.identity();
    private UnaryOperator<Document.Builder> documentProcessor = UnaryOperator.identity();
    private UnaryOperator<RuntimeWiring.Builder> runtimeWiringProcessor = UnaryOperator.identity();
    private SchemaGenerator.Options options = SchemaGenerator.Options.defaultOptions();
    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    public GraphQLSchemaBuilder addScalar(GraphQLScalarType scalarType, Class<?> javaType) {
        scalarTypeMap.put(scalarType, javaType);
        return this;
    }

    public GraphQLSchemaBuilder withArgumentFactory(ArgumentProviderFactory<?>... factories) {
        Collections.addAll(argumentProviderFactories, factories);
        return this;
    }

    public GraphQLSchemaBuilder withPackage(String... packages) {
        Collections.addAll(packageNames, packages);
        return this;
    }

    public GraphQLSchemaBuilder withType(Class<?>... classes) {
        Collections.addAll(types, classes);
        return this;
    }

    public GraphQLSchemaBuilder withFieldFilter(FieldVisibilityFilter... filters) {
        Collections.addAll(fieldFilters, filters);
        return this;
    }

    public GraphQLSchemaBuilder withTypeFilter(TypeVisibilityFilter... filters) {
        Collections.addAll(typeFilters, filters);
        return this;
    }

    public GraphQLSchemaBuilder withTypeDefinitionRegistry(TypeDefinitionRegistry... registries) {
        Collections.addAll(typeDefinitionRegistries, registries);
        return this;
    }

    public GraphQLSchemaBuilder codeRegistry(UnaryOperator<GraphQLCodeRegistry.Builder> builderUnaryOperator) {
        this.codeRegistryProcessor = checkNotNull(builderUnaryOperator);
        return this;
    }

    public GraphQLSchemaBuilder document(UnaryOperator<Document.Builder> builderUnaryOperator) {
        this.documentProcessor = checkNotNull(builderUnaryOperator);
        return this;
    }

    public GraphQLSchemaBuilder runtimeWiring(UnaryOperator<RuntimeWiring.Builder> builderUnaryOperator) {
        this.runtimeWiringProcessor = checkNotNull(builderUnaryOperator);
        return this;
    }

    public GraphQLSchemaBuilder serviceInstanceFactory(InstanceFactory instanceFactory) {
        this.instanceFactory = checkNotNull(instanceFactory);
        return this;
    }

    public GraphQLSchemaBuilder options(SchemaGenerator.Options options) {
        this.options = options;
        return this;
    }

    public GraphQLSchema build() {
        GraphQLDocumentBuilder documentBuilder = new GraphQLDocumentBuilder();
        GraphQLCodeRegistryBuilder codeRegistryBuilder = new GraphQLCodeRegistryBuilder(instanceFactory);
        GraphQLRuntimeWiringBuilder runtimeWiringBuilder = new GraphQLRuntimeWiringBuilder();

        Stream.concat(types.stream(), packageNames.stream().flatMap(this::searchPackage))
                .distinct()
                .forEach(cls -> {
                    documentBuilder.withType(cls);
                    codeRegistryBuilder.withType(cls);
                    runtimeWiringBuilder.withType(cls);
                });

        scalarTypeMap.forEach((scalar, type) -> {
            documentBuilder.addScalar(scalar, type);
            runtimeWiringBuilder.withScalar(scalar);
        });

        codeRegistryBuilder
                .setArgumentFactories(argumentProviderFactories)
                .setFieldFilters(fieldFilters)
                .setTypeFilters(typeFilters);

        Document document = documentProcessor.apply(documentBuilder.builder()).build();
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().buildRegistry(document);

        RuntimeWiring runtimeWiring = runtimeWiringProcessor.apply(runtimeWiringBuilder.builder())
                .codeRegistry(codeRegistryProcessor.apply(codeRegistryBuilder.builder()))
                .build();

        for (TypeDefinitionRegistry registry : typeDefinitionRegistries) {
            typeDefinitionRegistry = typeDefinitionRegistry.merge(registry);
        }

        return new SchemaGenerator().makeExecutableSchema(options, typeDefinitionRegistry, runtimeWiring);
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
