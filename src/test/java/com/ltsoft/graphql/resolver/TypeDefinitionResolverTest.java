package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.example.*;
import com.ltsoft.graphql.types.ScalarTypeRepository;
import graphql.language.*;
import graphql.schema.idl.SchemaPrinter;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeDefinitionResolverTest {

    @Test
    public void testScalars() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        typeDefinitionResolver.scalar(HelloObjectScalar.class, HelloObject.class);

        assertThat(typeDefinitionResolver.getTypeRepository().findMappingScalarType(HelloObject.class))
                .containsInstanceOf(HelloObjectScalar.class);
    }

    @Test
    public void testNormalObject() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        ObjectTypeDefinition typeDefinition = typeDefinitionResolver.object(NormalObject.class);

        assertThat(printDefinition(typeDefinitionResolver, typeDefinition))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/NormalObject.graphql"));
    }

    @Test
    public void testGraphQLView() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        ObjectTypeDefinition service = typeDefinitionResolver.object(MutationService.class);
        ObjectTypeDefinition object = typeDefinitionResolver.object(MutationObject.class);
        InputObjectTypeDefinition input = typeDefinitionResolver.input(MutationInputObject.class);

        assertThat(printDefinition(typeDefinitionResolver, service, object, input))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/MutationService.graphql"));
    }


    @Test
    public void testObjectExtension() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        ObjectTypeDefinition objectType = typeDefinitionResolver.object(NormalObject.class);
        ObjectTypeExtensionDefinition extObjectType = typeDefinitionResolver.extension(NormalObjectExtension.class);
        EnumTypeDefinition enumType = typeDefinitionResolver.enumeration(EnumObject.class);
        EnumTypeExtensionDefinition extEnumType = typeDefinitionResolver.extension(EnumObjectExtension.class);
        InterfaceTypeDefinition ifaceType = typeDefinitionResolver.iface(NormalInterface.class);
        InterfaceTypeExtensionDefinition extIfaceType = typeDefinitionResolver.extension(NormalInterfaceExtension.class);
        InputObjectTypeDefinition inputType = typeDefinitionResolver.input(MutationInputObject.class);
        InputObjectTypeExtensionDefinition extInputType = typeDefinitionResolver.extension(MutationInputObjectExtension.class);

        assertThat(printDefinition(typeDefinitionResolver, objectType, extObjectType, enumType, extEnumType, ifaceType, extIfaceType, inputType, extInputType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/NormalObjectExtension.graphql"));
    }

    @Test
    public void testObjectFieldExtension() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        ObjectTypeDefinition objectType = typeDefinitionResolver.object(ObjectWithExtension.class);

        assertThat(printDefinition(typeDefinitionResolver, objectType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/ObjectWithExtension.graphql"));
    }


    @Test
    public void testGraphQLInput() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        assertThat(MutationInputObject.class.isAnnotationPresent(GraphQLType.class)).isFalse();

        InputObjectTypeDefinition inputObject = typeDefinitionResolver.input(MutationInputObject.class);

        assertThat(printDefinition(typeDefinitionResolver, inputObject))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/MutationInputObject.graphql"));
    }


    @Test
    public void testGraphQLInterface() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        InterfaceTypeDefinition interfaceType = typeDefinitionResolver.iface(NormalInterface.class);

        assertThat(printDefinition(typeDefinitionResolver, interfaceType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/NormalInterface.graphql"));
    }

    @Test
    public void testGraphQLUnion() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        UnionTypeDefinition unionType = typeDefinitionResolver.union(UnionObject.class);
        ObjectTypeDefinition objectType = typeDefinitionResolver.object(NormalObject.class);
        ObjectTypeDefinition implType = typeDefinitionResolver.object(NormalInterfaceImpl.class);
        InterfaceTypeDefinition interfaceType = typeDefinitionResolver.iface(NormalInterface.class);

        assertThat(printDefinition(typeDefinitionResolver, unionType, objectType, implType, interfaceType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/UnionObject.graphql"));
    }

    @Test
    public void testGraphQLEnum() {
        TypeDefinitionResolver typeDefinitionResolver = new TypeDefinitionResolver();

        EnumTypeDefinition enumObject = typeDefinitionResolver.enumeration(EnumObject.class);

        assertThat(printDefinition(typeDefinitionResolver, enumObject))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/EnumObject.graphql"));
    }

    private String readSchemaExample(String path) {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(getClass().getResource(path).toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String printDefinition(TypeDefinitionResolver typeDefinitionResolver, Definition... definitions) {
        ScalarTypeRepository typeRepository = typeDefinitionResolver.getTypeRepository();
        SchemaPrinter printer = new SchemaPrinter();

        Definition rootType = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("schema")
                .fieldDefinition(FieldDefinition.newFieldDefinition().name("query").type(new TypeName("Query")).build())
                .build();

        Definition queryType = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition().name("hello").type(new TypeName("String")).build())
                .build();

        Document.Builder documentBuilder = Document.newDocument()
                .definition(rootType)
                .definition(queryType);

        Arrays.asList(definitions).forEach(documentBuilder::definition);

        typeRepository.allExtensionTypeDefinitions().forEach(documentBuilder::definition);

        return printer.print(documentBuilder.build());
    }
}
