package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.ArgumentProviderFactory;
import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.TypeResolver;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import com.ltsoft.graphql.provider.TypeNameProvider;
import com.ltsoft.graphql.scalars.ScalarTypeRepository;
import graphql.language.Definition;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.GraphQLScalarType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.ltsoft.graphql.resolver.ResolveUtil.isGraphQLObjectLikeType;
import static java.util.Objects.requireNonNull;

public final class TypeProviderFactory {

    private final Stack<Type> unprocessedTypes = new Stack<>();
    private final Set<Type> registeredTypes = new HashSet<>();
    private final Set<Type> resolvingTypes = new HashSet<>();
    private final Map<Type, TypeProvider<?>> providerCache = new HashMap<>();

    private final List<TypeResolver<?>> typeResolvers = new ArrayList<>();

    private List<ArgumentProviderFactory<?>> argumentProviderFactories = Collections.emptyList();
    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    public void addClass(Class<?> type) {
        addType(type);
    }

    public void addScalar(GraphQLScalarType scalarType, Class<?> javaType) {
        ScalarTypeRepository.getInstance().register(javaType, scalarType);
        addType(javaType);
    }

    public void addTypeResolvers(TypeResolver<?>... resolvers) {
        Collections.addAll(typeResolvers, resolvers);
    }

    public void setArgumentFactories(List<ArgumentProviderFactory<?>> factories) {
        this.argumentProviderFactories = requireNonNull(factories);
    }

    public void setInstanceFactory(InstanceFactory instanceFactory) {
        this.instanceFactory = requireNonNull(instanceFactory);
    }

    Stream<TypeProvider<?>> getProviders(Collection<TypeResolver<?>> resolvers) {
        typeResolvers.addAll(resolvers);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TypeProviderIterator(), Spliterator.IMMUTABLE), false)
                .filter(Objects::nonNull);
    }

    public Stream<TypeProvider<?>> getProviders() {
        return getProviders(initializeDefaultResolvers());
    }

    private List<TypeResolver<?>> initializeDefaultResolvers() {
        List<TypeResolver<?>> resolvers = new ArrayList<>();
        resolvers.add(new DirectiveTypeResolver());
        resolvers.add(new EnumTypeResolver());
        resolvers.add(new InputObjectTypeResolver());
        resolvers.add(new InterfaceTypeResolver(instanceFactory));
        resolvers.add(new ObjectTypeResolver(instanceFactory, argumentProviderFactories));
        resolvers.add(new ScalarTypeResolver());
        resolvers.add(new UnionTypeResolver(instanceFactory));
        return resolvers;
    }

    private void addType(Type javaType) {
        if (registeredTypes.add(javaType)) {
            unprocessedTypes.add(javaType);
        }
    }

    private TypeProvider<?> processType(Type javaType) {
        if (providerCache.containsKey(javaType)) {
            return providerCache.get(javaType);
        }

        TypeProvider<?> provider = typeResolvers.stream()
                .filter(ele -> ele.isSupport(javaType))
                .findFirst()
                .map(ele -> resolveProvider(ele, javaType))
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Can not find TypeResolver for type '%s'", javaType))
                );

        providerCache.put(javaType, provider);

        return provider;
    }

    private TypeProvider<?> resolveProvider(TypeResolver<?> ele, Type type) {
        TypeProvider<?> provider = null;

        if (resolvingTypes.add(type)) {

            provider = ele.resolve(type, this::resolveAndRegister);

            resolvingTypes.remove(type);
        } else if (type instanceof Class) {
            Class<?> cls = (Class<?>) type;

            if (isGraphQLObjectLikeType(cls)) {
                provider = new TypeNameProvider(cls);
            }
        }

        if (provider == null) {
            throw new IllegalStateException(String.format("Will trigger cycle loading when resolve type '%s'", type));
        }

        return provider;
    }

    private TypeProvider<?> resolveAndRegister(Type javaType) {
        addType(javaType);
        return processType(javaType);
    }

    private class TypeProviderIterator implements Iterator<TypeProvider<?>> {

        Set<String> registeredScalarType = new HashSet<>();

        @Override
        public boolean hasNext() {
            return !unprocessedTypes.isEmpty();
        }

        @Override
        public TypeProvider<?> next() {
            TypeProvider<?> provider = processType(unprocessedTypes.pop());

            Definition<?> definition = provider.getDefinition();
            if (definition instanceof ScalarTypeDefinition) {
                //存在多个 Java 类型解析为同一个 Scalar 类型的情况，需要在迭代时忽略掉，由后面的空值过滤器去除
                if (!registeredScalarType.add(((ScalarTypeDefinition) definition).getName())) {
                    return null;
                }
            }

            return provider;
        }

    }
}
