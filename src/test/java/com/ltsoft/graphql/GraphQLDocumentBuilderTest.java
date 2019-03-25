package com.ltsoft.graphql;

import com.ltsoft.graphql.example.*;
import graphql.language.Document;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQLDocumentBuilderTest {

    @Test
    public void build() {
        Document document = new GraphQLDocumentBuilder()
                .addScalar(HelloObject.HelloObjectScalar, HelloObject.class)
                .withType(EnumObject.class)
                .withType(EnumObjectExtension.class)
                .withType(NormalObject.class)
                .withType(NormalInterface.class)
                .withType(MutationInputObject.class)
                .withType(NormalDirective.class)
                .withType(UnionObject.class)
                .builder()
                .build();

        assertThat(document).isNotNull();
        assertThat(document.getDefinitions()).isNotEmpty();
    }
}
