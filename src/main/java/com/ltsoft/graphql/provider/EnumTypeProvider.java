package com.ltsoft.graphql.provider;

import com.ltsoft.graphql.TypeProvider;
import graphql.language.Definition;
import graphql.language.Description;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.schema.idl.EnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.ltsoft.graphql.resolver.ResolveUtil.EMPTY_SOURCE_LOCATION;
import static java.util.Objects.requireNonNull;

public class EnumTypeProvider implements TypeProvider<EnumTypeDefinition> {

    private final EnumTypeDefinition definition;
    private final EnumValuesProvider valueProvider;

    public EnumTypeProvider(EnumTypeDefinition definition, EnumValuesProvider provider) {
        this.definition = definition;
        this.valueProvider = provider;
    }

    @SuppressWarnings("unused")
    public static EnumTypeProvider.Builder newEnumTypeProvider() {
        return new Builder();
    }

    @Override
    public Definition<EnumTypeDefinition> getDefinition() {
        return definition;
    }

    @Override
    public UnaryOperator<RuntimeWiring.Builder> getWiringOperator() {
        return builder -> builder.type(definition.getName(), type -> type.enumValues(valueProvider));
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Builder {

        private String name;
        private String description;

        private final Map<String, Object> valueMap = new HashMap<>();
        private final List<EnumValueDefinition> valueDefinitions = new ArrayList<>();

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder enumValues(String name, Object value) {
            return enumValues(name, value, null);
        }

        public Builder enumValues(String name, Object value, String description) {
            EnumValueDefinition valueDefinition = EnumValueDefinition.newEnumValueDefinition()
                    .name(name)
                    .description(new Description(description, EMPTY_SOURCE_LOCATION, false))
                    .build();

            valueDefinitions.add(valueDefinition);
            valueMap.put(name, value);

            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public EnumTypeProvider build() {

            EnumTypeDefinition definition = EnumTypeDefinition.newEnumTypeDefinition()
                    .description(new Description(description, EMPTY_SOURCE_LOCATION, false))
                    .enumValueDefinitions(valueDefinitions)
                    .name(requireNonNull(name))
                    .build();

            return new EnumTypeProvider(definition, new EnumFieldValueProvider(definition.getName(), valueMap));
        }
    }

}
