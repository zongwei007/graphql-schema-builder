package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.example.*;
import com.ltsoft.graphql.scalars.ScalarTypeRepository;
import graphql.language.*;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.SchemaPrinter;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class DefinitionResolverTest {

    @Test
    public void testScalars() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        definitionResolver.scalar(HelloObject.HelloObjectScalar, HelloObject.class);

        assertThat(definitionResolver.getTypeRepository().findMappingScalarType(HelloObject.class))
                .containsInstanceOf(GraphQLScalarType.class);
    }

    @Test
    public void testNormalObject() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        ObjectTypeDefinition typeDefinition = definitionResolver.object(NormalObject.class);

        assertThat(printDefinition(definitionResolver, typeDefinition))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/NormalObject.graphql"));
    }

    @Test
    public void testArgumentService() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        ObjectTypeDefinition argumentObject = definitionResolver.object(ArgumentService.class);
        ObjectTypeDefinition mutationObject = definitionResolver.object(MutationObject.class);
        InputObjectTypeDefinition inputObject = definitionResolver.input(MutationInputObject.class);

        assertThat(printDefinition(definitionResolver, argumentObject, mutationObject, inputObject))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/ArgumentService.graphql"));
    }

    @Test
    public void testGraphQLView() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        ObjectTypeDefinition service = definitionResolver.object(MutationService.class);
        ObjectTypeDefinition object = definitionResolver.object(MutationObject.class);
        InputObjectTypeDefinition input = definitionResolver.input(MutationInputObject.class);

        assertThat(printDefinition(definitionResolver, service, object, input))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/MutationService.graphql"));
    }

    @Test
    public void testGeneric() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        ObjectTypeDefinition service = definitionResolver.object(GenericServiceImpl.class);
        ObjectTypeDefinition object = definitionResolver.object(MutationObject.class);
        InputObjectTypeDefinition input = definitionResolver.input(MutationInputObject.class);

        assertThat(printDefinition(definitionResolver, service, object, input))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/GenericServiceImpl.graphql"));
    }

    @Test
    public void testObjectExtension() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        ObjectTypeDefinition objectType = definitionResolver.object(NormalInterfaceImpl.class);
        ObjectTypeExtensionDefinition extObjectType = definitionResolver.extension(NormalInterfaceImplExtension.class);

        EnumTypeDefinition enumType = definitionResolver.enumeration(EnumObject.class);
        EnumTypeExtensionDefinition extEnumType = definitionResolver.extension(EnumObjectExtension.class);

        InterfaceTypeDefinition ifaceType = definitionResolver.iface(NormalInterface.class);
        InterfaceTypeExtensionDefinition extIfaceType = definitionResolver.extension(NormalInterfaceExtension.class);

        InputObjectTypeDefinition inputType = definitionResolver.input(MutationInputObject.class);
        InputObjectTypeExtensionDefinition extInputType = definitionResolver.extension(MutationInputObjectExtension.class);

        UnionTypeDefinition unionType = definitionResolver.union(UnionObject.class);
        ObjectTypeDefinition normalObject = definitionResolver.object(NormalObject.class);
        ObjectTypeDefinition mutationObject = definitionResolver.object(MutationObject.class);
        UnionTypeExtensionDefinition extUnionType = definitionResolver.extension(UnionObjectWithExtension.class);

        assertThat(printDefinition(definitionResolver, objectType, extObjectType, enumType, extEnumType,
                ifaceType, extIfaceType, inputType, extInputType, unionType, normalObject, mutationObject, extUnionType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/NormalObjectExtension.graphql"));
    }

    @Test
    public void testObjectFieldExtension() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        ObjectTypeDefinition objectType = definitionResolver.object(ObjectWithExtension.class);

        assertThat(printDefinition(definitionResolver, objectType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/ObjectWithExtension.graphql"));
    }


    @Test
    public void testGraphQLInput() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        assertThat(MutationInputObject.class.isAnnotationPresent(GraphQLType.class)).isFalse();

        InputObjectTypeDefinition inputObject = definitionResolver.input(MutationInputObject.class);

        assertThat(printDefinition(definitionResolver, inputObject))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/MutationInputObject.graphql"));
    }


    @Test
    public void testGraphQLInterface() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        InterfaceTypeDefinition interfaceType = definitionResolver.iface(NormalInterface.class);

        assertThat(printDefinition(definitionResolver, interfaceType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/NormalInterface.graphql"));
    }

    @Test
    public void testGraphQLUnion() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        UnionTypeDefinition unionType = definitionResolver.union(UnionObject.class);
        ObjectTypeDefinition objectType = definitionResolver.object(NormalObject.class);
        ObjectTypeDefinition implType = definitionResolver.object(NormalInterfaceImpl.class);
        InterfaceTypeDefinition interfaceType = definitionResolver.iface(NormalInterface.class);

        assertThat(printDefinition(definitionResolver, unionType, objectType, implType, interfaceType))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/UnionObject.graphql"));
    }

    @Test
    public void testGraphQLEnum() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        EnumTypeDefinition enumObject = definitionResolver.enumeration(EnumObject.class);

        assertThat(printDefinition(definitionResolver, enumObject))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/EnumObject.graphql"));
    }

    @Test
    public void testDirective() {
        DefinitionResolver definitionResolver = new DefinitionResolver();

        DirectiveDefinition directiveDefinition = definitionResolver.directive(NormalDirective.class);
        InterfaceTypeDefinition example = definitionResolver.iface(NormalDirectiveExample.class);

        assertThat(printDefinition(definitionResolver, directiveDefinition, example))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/NormalDirective.graphql"));
    }

    private String readSchemaExample(String path) {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(getClass().getResource(path).toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String printDefinition(DefinitionResolver definitionResolver, Definition... definitions) {
        ScalarTypeRepository typeRepository = definitionResolver.getTypeRepository();
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

        Document schemaIDL = documentBuilder.build();

        return printer.print(schemaIDL);
    }
}
