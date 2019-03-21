package com.ltsoft.graphql.types;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import graphql.Scalars;
import graphql.language.ScalarTypeDefinition;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.util.*;

public class ScalarTypeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalarTypeRepository.class);

    private LoadingCache<Class<?>, Optional<GraphQLScalarType>> MAPPING_CACHE = CacheBuilder.newBuilder()
            .build(new ClassGraphQLScalarTypeCacheLoader());

    private Map<String, GraphQLScalarType> scalarTypeMap = new HashMap<>();
    private Map<String, ScalarTypeDefinition> typeDefinitionMap = new HashMap<>();
    private BiMap<Class<?>, GraphQLScalarType> javaTypeMap = HashBiMap.create();

    public ScalarTypeRepository() {
        register(ExtendedScalars.Json);
        register(ExtendedScalars.Object);
        //ExtendedScalars.URL 类型依赖了 okhttp3，感觉太重了……

        register(LocalDate.class, ExtendedScalars.Date);
        register(OffsetDateTime.class, ExtendedScalars.DateTime);
        register(OffsetTime.class, ExtendedScalars.Time);

        register(UUID.class, ScalarTypes.GraphQLUUID);
        register(URI.class, ScalarTypes.GraphQLURI);
        register(Instant.class, ScalarTypes.GraphQLInstant);
        register(LocalTime.class, ScalarTypes.GraphQLLocalTime);
        register(LocalDateTime.class, ScalarTypes.GraphQLLocalDateTime);
        register(ZonedDateTime.class, ScalarTypes.GraphQLZonedDateTime);
        register(Duration.class, ScalarTypes.GraphQLDuration);
        register(Period.class, ScalarTypes.GraphQLPeriod);
        register(Year.class, ScalarTypes.GraphQLYear);
        register(YearMonth.class, ScalarTypes.GraphQLYearMonth);

        //类型映射
        mapping(Integer.class, Scalars.GraphQLInt);
        mapping(Double.class, Scalars.GraphQLFloat);
        mapping(String.class, Scalars.GraphQLString);
        mapping(Boolean.class, Scalars.GraphQLBoolean);
        mapping(Long.class, Scalars.GraphQLLong);
        mapping(Short.class, Scalars.GraphQLShort);
        mapping(Byte.class, Scalars.GraphQLByte);
        mapping(BigInteger.class, Scalars.GraphQLBigInteger);
        mapping(BigDecimal.class, Scalars.GraphQLBigDecimal);
        mapping(Character.class, Scalars.GraphQLChar);
    }

    public ScalarTypeRepository register(GraphQLScalarType... types) {
        for (GraphQLScalarType type : types) {
            if (scalarTypeMap.putIfAbsent(type.getName(), type) != null) {
                LOGGER.warn("GraphQLScalarType {} has been registered", type.getName());
            }

            typeDefinitionMap.put(type.getName(), ScalarTypeDefinition.newScalarTypeDefinition().name(type.getName()).build());
        }
        return this;
    }

    public ScalarTypeRepository mapping(Class<?> sourceType, GraphQLScalarType scalarType) {
        javaTypeMap.put(sourceType, scalarType);
        return this;
    }

    public ScalarTypeRepository register(Class<?> sourceType, GraphQLScalarType scalarType) {
        mapping(sourceType, scalarType);
        register(scalarType);
        return this;
    }

    public Optional<GraphQLScalarType> getScalarType(String name) {
        return Optional.ofNullable(scalarTypeMap.get(name));
    }

    public Optional<ScalarTypeDefinition> getScalarTypeDefinition(String name) {
        return Optional.ofNullable(typeDefinitionMap.get(name));
    }

    public Optional<GraphQLScalarType> findMappingScalarType(Class<?> cls) {
        return MAPPING_CACHE.getUnchecked(cls);
    }

    public Set<GraphQLScalarType> allExtensionTypes() {
        return new HashSet<>(scalarTypeMap.values());
    }

    public Set<ScalarTypeDefinition> allExtensionTypeDefinitions() {
        return new HashSet<>(typeDefinitionMap.values());
    }

    private class ClassGraphQLScalarTypeCacheLoader extends CacheLoader<Class<?>, Optional<GraphQLScalarType>> {
        @Override
        public Optional<GraphQLScalarType> load(Class<?> cls) {
            if (javaTypeMap.containsKey(cls)) {
                return Optional.of(javaTypeMap.get(cls));
            }

            return javaTypeMap.keySet().stream()
                    .filter(parentCls -> parentCls.isAssignableFrom(cls))
                    .findFirst()
                    .map(parentType -> javaTypeMap.get(parentType));
        }
    }
}
