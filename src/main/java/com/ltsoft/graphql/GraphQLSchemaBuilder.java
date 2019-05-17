package com.ltsoft.graphql;

import com.google.common.reflect.ClassPath;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import com.ltsoft.graphql.resolver.ResolveUtil;
import com.ltsoft.graphql.resolver.TypeProviderFactory;
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
    private final List<ArgumentConverter<?>> argumentConverters = new ArrayList<>();
    private final List<TypeDefinitionRegistry> typeDefinitionRegistries = new ArrayList<>();
    private final List<TypeResolver<?>> typeResolvers = new ArrayList<>();
    private final List<TypeProvider<?>> typeProviders = new ArrayList<>();
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

    public GraphQLSchemaBuilder argumentConverter(ArgumentConverter<?>... converters) {
        Collections.addAll(argumentConverters, converters);
        return this;
    }

    public GraphQLSchemaBuilder typeResolver(TypeResolver<?>... resolvers) {
        Collections.addAll(typeResolvers, resolvers);
        return this;
    }

    public GraphQLSchemaBuilder typeProvider(TypeProvider<?>... providers) {
        Collections.addAll(typeProviders, providers);
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
        Document.Builder documentBuilder = Document.newDocument();
        RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();

        TypeProviderFactory factory = new TypeProviderFactory();
        factory.setArgumentProviderFactories(argumentProviderFactories);
        factory.setArgumentConverters(argumentConverters);
        factory.setInstanceFactory(instanceFactory);

        scalarTypeMap.forEach(factory::addScalar);
        typeResolvers.forEach(factory::addTypeResolvers);
        Stream.concat(types.stream(), packageNames.stream().flatMap(this::searchPackage))
                .distinct()
                .forEach(factory::addClass);

        Stream.concat(factory.getProviders(), typeProviders.stream()).forEach(provider -> {
            provider.getDefinitionOperator().apply(documentBuilder);
            provider.getWiringOperator().apply(runtimeWiringBuilder);
        });

        Document document = combineUnaryOperator(documentProcessors).apply(documentBuilder).build();
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().buildRegistry(document);

        for (TypeDefinitionRegistry registry : typeDefinitionRegistries) {
            typeDefinitionRegistry = typeDefinitionRegistry.merge(registry);
        }

        RuntimeWiring runtimeWiring = combineUnaryOperator(runtimeWiringProcessors).apply(runtimeWiringBuilder).build();

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
