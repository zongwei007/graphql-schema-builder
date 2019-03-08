package com.ltsoft.graphql.types;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScalarTypeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalarTypeRepository.class);

    private Map<String, GraphQLScalarType> scalarTypeMap = new ConcurrentHashMap<>();

    public ScalarTypeRepository() {
        register(ExtendedScalars.Date);
        register(ExtendedScalars.DateTime);
        register(ExtendedScalars.Time);
        register(ExtendedScalars.Json);
        register(ExtendedScalars.Object);
        //ExtendedScalars.URL 类型依赖了 okhttp3，感觉太重了……

        register(ScalarTypes.GraphQLUUID);
        register(ScalarTypes.GraphQLURI);
        register(ScalarTypes.GraphQLInstant);
        register(ScalarTypes.GraphQLLocalTime);
        register(ScalarTypes.GraphQLLocalDateTime);
        register(ScalarTypes.GraphQLZonedDateTime);
        register(ScalarTypes.GraphQLDuration);
        register(ScalarTypes.GraphQLPeriod);
        register(ScalarTypes.GraphQLYear);
        register(ScalarTypes.GraphQLYearMonth);
    }

    public ScalarTypeRepository register(GraphQLScalarType... types) {
        for (GraphQLScalarType type : types) {
            if (scalarTypeMap.putIfAbsent(type.getName(), type) != null) {
                LOGGER.warn("GraphQLScalarType {} has been registered", type.getName());
            }
        }
        return this;
    }

    public Optional<GraphQLScalarType> get(String name) {
        return Optional.ofNullable(scalarTypeMap.get(name));
    }

    public Set<GraphQLScalarType> allExtensionTypes() {
        return new HashSet<>(scalarTypeMap.values());
    }

}
