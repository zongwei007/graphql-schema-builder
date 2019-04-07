package com.ltsoft.graphql.scalars;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ScalarTypeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalarTypeRepository.class);

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_MAP = new HashMap<>();

    static {
        PRIMITIVE_TYPE_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_TYPE_MAP.put(byte.class, Byte.class);
        PRIMITIVE_TYPE_MAP.put(short.class, Short.class);
        PRIMITIVE_TYPE_MAP.put(int.class, Integer.class);
        PRIMITIVE_TYPE_MAP.put(long.class, Long.class);
        PRIMITIVE_TYPE_MAP.put(float.class, Float.class);
        PRIMITIVE_TYPE_MAP.put(double.class, Double.class);
    }

    private static class SingletonHolder {
        private static final ScalarTypeRepository INSTANCE = new ScalarTypeRepository();
    }

    public static ScalarTypeRepository getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private LoadingCache<Class<?>, Optional<GraphQLScalarType>> MAPPING_CACHE = CacheBuilder.newBuilder()
            .build(new ClassGraphQLScalarTypeCacheLoader());

    private Map<String, GraphQLScalarType> scalarTypeMap = new HashMap<>();
    private Map<Class<?>, GraphQLScalarType> javaTypeMap = new HashMap<>();

    private ScalarTypeRepository() {
        register(Map.class, ExtendedScalars.Object);
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
        mapping(Byte.class, Scalars.GraphQLByte);
        mapping(Boolean.class, Scalars.GraphQLBoolean);
        mapping(Short.class, Scalars.GraphQLShort);
        mapping(Integer.class, Scalars.GraphQLInt);
        mapping(Long.class, Scalars.GraphQLLong);
        mapping(Float.class, Scalars.GraphQLFloat);
        mapping(Double.class, Scalars.GraphQLFloat);
        mapping(Character.class, Scalars.GraphQLChar);
        mapping(String.class, Scalars.GraphQLString);
        mapping(BigInteger.class, Scalars.GraphQLBigInteger);
        mapping(BigDecimal.class, Scalars.GraphQLBigDecimal);
    }

    void register(GraphQLScalarType... types) {
        for (GraphQLScalarType type : types) {
            if (scalarTypeMap.putIfAbsent(type.getName(), type) != null) {
                LOGGER.warn("GraphQLScalarType {} has been registered", type.getName());
            }
        }
    }

    private void mapping(Class<?> sourceType, GraphQLScalarType scalarType) {
        javaTypeMap.put(sourceType, scalarType);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ScalarTypeRepository register(Class<?> sourceType, GraphQLScalarType scalarType) {
        mapping(sourceType, scalarType);
        register(scalarType);

        MAPPING_CACHE.invalidate(sourceType);
        return this;
    }

    public Optional<GraphQLScalarType> getScalarType(String name) {
        return Optional.ofNullable(scalarTypeMap.get(name));
    }

    public Optional<GraphQLScalarType> findMappingScalarType(Class<?> cls) {
        return MAPPING_CACHE.getUnchecked(cls.isPrimitive() ? PRIMITIVE_TYPE_MAP.get(cls) : cls);
    }

    private class ClassGraphQLScalarTypeCacheLoader extends CacheLoader<Class<?>, Optional<GraphQLScalarType>> {
        @Override
        public Optional<GraphQLScalarType> load(@SuppressWarnings("NullableProblems") Class<?> cls) {
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
