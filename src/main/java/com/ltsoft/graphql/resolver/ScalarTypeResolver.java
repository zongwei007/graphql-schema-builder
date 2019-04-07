package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.TypeResolver;
import com.ltsoft.graphql.provider.ScalarTypeProvider;
import com.ltsoft.graphql.scalars.ScalarTypeRepository;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.GraphQLScalarType;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

public class ScalarTypeResolver implements TypeResolver<ScalarTypeDefinition> {

    @Override
    public boolean isSupport(Type javaType) {
        //noinspection UnstableApiUsage
        return findMappingScalarType(TypeToken.of(javaType).getRawType()).isPresent();
    }

    @Override
    public TypeProvider<ScalarTypeDefinition> resolve(Type javaType, Function<Type, TypeProvider<?>> resolver) {
        //noinspection UnstableApiUsage
        GraphQLScalarType type = findMappingScalarType(TypeToken.of(javaType).getRawType())
                .orElse(null);

        checkArgument(type != null);

        return new ScalarTypeProvider(type);
    }

    private Optional<GraphQLScalarType> findMappingScalarType(Class<?> javaType) {
        return ScalarTypeRepository.getInstance().findMappingScalarType(javaType);
    }

}
