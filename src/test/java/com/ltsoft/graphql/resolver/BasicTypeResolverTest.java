package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.TypeResolver;
import graphql.language.*;
import graphql.schema.idl.SchemaPrinter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public abstract class BasicTypeResolverTest {

    String readSchemaExample(String path) {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(BasicTypeResolverTest.class.getResource(path).toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    String printDefinition(Class<?> target, TypeResolver<?>... resolvers) {
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

        TypeProviderFactory factory = new TypeProviderFactory();
        factory.addClass(target);

        factory.getProviders(Arrays.asList(resolvers))
                .map(TypeProvider::getDefinitionOperator)
                .forEach(operator -> operator.apply(documentBuilder));

        Document schemaIDL = documentBuilder.build();

        return printDocument(schemaIDL);
    }

    @SuppressWarnings("WeakerAccess")
    String printDocument(Document document) {
        SchemaPrinter printer = new SchemaPrinter();

        return printer.print(document);
    }
}
