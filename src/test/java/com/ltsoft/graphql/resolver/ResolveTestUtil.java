package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.scalars.ScalarTypeRepository;
import graphql.language.*;
import graphql.schema.idl.SchemaPrinter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ResolveTestUtil {

    public static EnumTypeDefinition buildCustomEnumDefinition() {
        return EnumTypeDefinition.newEnumTypeDefinition()
                .name("CustomEnum")
                .enumValueDefinition(EnumValueDefinition.newEnumValueDefinition()
                        .name("customFirst")
                        .build()
                )
                .enumValueDefinition(EnumValueDefinition.newEnumValueDefinition()
                        .name("customSecond")
                        .build()
                )
                .build();
    }

    static String readSchemaExample(String path) {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(ResolveTestUtil.class.getResource(path).toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    static String printDefinition(DefinitionResolver definitionResolver, Definition... definitions) {
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
