package com.ltsoft.graphql.resolver;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.provider.EnumFieldValueProvider;
import com.ltsoft.graphql.provider.EnumTypeProvider;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class EnumTypeResolver extends BasicTypeResolver<EnumTypeDefinition> {

    private final Map<String, EnumFieldValueProvider> enumValueProviders = new HashMap<>();

    @Override
    TypeProvider<EnumTypeDefinition> resolve(Class<?> cls, Function<Type, TypeProvider<?>> resolver) {
        List<EnumValueDefinition> definitions = Arrays.stream(cls.getFields())
                .map(field -> new FieldInformation(cls, null, field))
                .filter(FieldInformation::isNotIgnore)
                .map(info ->
                        EnumValueDefinition.newEnumValueDefinition()
                                .comments(info.getComments())
                                .description(info.getDescription())
                                .directives(info.getDirectives(resolver))
                                .name(info.getName())
                                .sourceLocation(info.getSourceLocation())
                                .build()
                )
                .collect(Collectors.toList());

        EnumTypeDefinition definition = EnumTypeDefinition.newEnumTypeDefinition()
                .comments(getComment(cls))
                .description(getDescription(cls))
                .directives(getDirective(cls, resolver))
                .enumValueDefinitions(definitions)
                .name(resolveTypeName(cls))
                .sourceLocation(getSourceLocation(cls))
                .build();

        EnumFieldValueProvider valueProvider = new EnumFieldValueProvider(cls);
        EnumFieldValueProvider merged = enumValueProviders.compute(valueProvider.getTypeName(), (key, existed) ->
                existed == null ? valueProvider : valueProvider.merge(existed)
        );

        return new EnumTypeProvider(definition, merged);
    }

    @Override
    public boolean isSupport(Type javaType) {
        //noinspection UnstableApiUsage
        return ResolveUtil.isGraphQLEnumType(TypeToken.of(javaType).getRawType());
    }
}
