package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.annotations.GraphQLUnion;
import com.ltsoft.graphql.provider.UnionTypeProvider;
import graphql.language.TypeName;
import graphql.language.UnionTypeDefinition;
import graphql.schema.TypeResolver;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ltsoft.graphql.resolver.ResolveUtil.*;

public class UnionTypeResolver extends BasicTypeResolver<UnionTypeDefinition> {

    private InstanceFactory instanceFactory;

    UnionTypeResolver(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    @Override
    public boolean isSupport(Type javaType) {
        //noinspection UnstableApiUsage
        Class<?> cls = TypeToken.of(javaType).getRawType();

        return cls.isInterface() && ResolveUtil.hasGraphQLAnnotation(cls, GraphQLUnion.class);
    }

    @Override
    TypeProvider<UnionTypeDefinition> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        TypeResolver typeResolver = Optional.ofNullable(cls.getAnnotation(GraphQLUnion.class))
                .map(GraphQLUnion::typeResolver)
                .map(ele -> instanceFactory.provide(ele))
                .orElse(null);

        List<graphql.language.Type> possibleTypes = Arrays.stream(cls.getAnnotation(GraphQLUnion.class).possibleTypes())
                .map(resolver::apply)
                .map(TypeProvider::getTypeName)
                .map(TypeName::new)
                .collect(Collectors.toList());

        UnionTypeDefinition definition = UnionTypeDefinition.newUnionTypeDefinition()
                .comments(resolveComment(cls))
                .description(resolveDescription(cls))
                .directives(resolveDirective(cls, resolver))
                .memberTypes(possibleTypes)
                .name(resolveTypeName(cls))
                .sourceLocation(resolveSourceLocation(cls))
                .build();

        return new UnionTypeProvider(definition, typeResolver);
    }
}
