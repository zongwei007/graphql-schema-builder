package com.ltsoft.graphql.example.custom;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.TypeResolver;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class CustomTypeResolver implements TypeResolver<ObjectTypeDefinition> {

    @Override
    public boolean isSupport(Type javaType) {
        return TypeToken.of(javaType).isSubtypeOf(CustomType.class);
    }

    @Override
    public TypeProvider<ObjectTypeDefinition> resolve(Type javaType, Function<Type, TypeProvider<?>> resolver) {
        TypeVariable<Class<CustomType>> typeVariable = CustomType.class.getTypeParameters()[0];
        Class<?> parameterType = TypeToken.of(javaType).resolveType(typeVariable).getRawType();

        ObjectTypeDefinition definition = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Custom" + parameterType.getSimpleName() + "Type")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("info")
                        .type(new TypeName(parameterType.getSimpleName()))
                        .build()
                )
                .build();

        return () -> definition;
    }
}
