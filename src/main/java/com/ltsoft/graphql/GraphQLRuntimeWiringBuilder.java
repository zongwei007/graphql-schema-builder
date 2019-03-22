package com.ltsoft.graphql;

import com.ltsoft.graphql.annotations.GraphQLType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.EnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class GraphQLRuntimeWiringBuilder {

    private final Set<GraphQLScalarType> scalarTypeSet = new HashSet<>();
    private final List<EnumOperator> enumOperators = new ArrayList<>();

    public GraphQLRuntimeWiringBuilder withScalar(GraphQLScalarType... scalarTypes) {
        Collections.addAll(scalarTypeSet, scalarTypes);
        return this;
    }

    public GraphQLRuntimeWiringBuilder withType(Class<?>... classes) {
        Arrays.stream(classes)
                .filter(Class::isEnum)
                .filter(cls -> cls.isAnnotationPresent(GraphQLType.class))
                .map(EnumOperator::new)
                .forEach(enumOperators::add);

        return this;
    }

    public RuntimeWiring.Builder builder() {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

        scalarTypeSet.forEach(builder::scalar);

        for (EnumOperator operator : enumOperators) {
            builder = builder.type(operator.getTypeName(), operator);
        }

        return builder;
    }

    private class EnumOperator implements UnaryOperator<TypeRuntimeWiring.Builder> {

        private final EnumFieldProvider valuesProvider;
        private final String typeName;

        public EnumOperator(Class<?> cls) {
            this.typeName = resolveTypeName(cls);
            this.valuesProvider = new EnumFieldProvider(cls);
        }

        @Override
        public TypeRuntimeWiring.Builder apply(TypeRuntimeWiring.Builder builder) {
            return builder.enumValues(valuesProvider);
        }

        public String getTypeName() {
            return typeName;
        }
    }

    private class EnumFieldProvider implements EnumValuesProvider {

        private final Map<String, ?> valueMap;

        public EnumFieldProvider(Class<?> type) {
            this.valueMap = Arrays.stream(type.getEnumConstants())
                    .collect(Collectors.toMap(Object::toString, Function.identity()));
        }

        @Override
        public Object getValue(String name) {
            return valueMap.get(name);
        }
    }
}
