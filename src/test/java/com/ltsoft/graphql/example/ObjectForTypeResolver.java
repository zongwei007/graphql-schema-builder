package com.ltsoft.graphql.example;

import com.google.common.reflect.TypeToken;
import com.ltsoft.graphql.TypeResolver;
import com.ltsoft.graphql.annotations.GraphQLType;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

@GraphQLType
public class ObjectForTypeResolver {

    private List<CustomType<String>> customType;

    public List<CustomType<String>> getCustomType() {
        return customType;
    }

    public void setCustomType(List<CustomType<String>> customType) {
        this.customType = customType;
    }

    public static class CustomType<T> {

        private T info;

        public T getInfo() {
            return info;
        }

        public void setInfo(T info) {
            this.info = info;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class CustomTypeResolver implements TypeResolver<ObjectTypeDefinition> {

        @Override
        public boolean isSupport(Type javaType) {
            return TypeToken.of(javaType).isSubtypeOf(CustomType.class);
        }

        @Override
        public TypeDefinition<ObjectTypeDefinition> resolve(Type javaType) {
            TypeVariable<Class<CustomType>> typeVariable = CustomType.class.getTypeParameters()[0];
            Class<?> parameterType = TypeToken.of(javaType).resolveType(typeVariable).getRawType();

            return ObjectTypeDefinition.newObjectTypeDefinition()
                    .name("Custom" + parameterType.getSimpleName() + "Type")
                    .fieldDefinition(FieldDefinition.newFieldDefinition()
                            .name("info")
                            .type(new TypeName(parameterType.getSimpleName()))
                            .build()
                    )
                    .build();
        }
    }

}
