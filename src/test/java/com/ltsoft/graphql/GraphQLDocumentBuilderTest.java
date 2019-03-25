package com.ltsoft.graphql;

import com.ltsoft.graphql.example.EnumObject;
import com.ltsoft.graphql.example.EnumObjectExtension;
import graphql.language.Document;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQLDocumentBuilderTest {

    @Test
    public void build() {
        Document document = new GraphQLDocumentBuilder()
                .withType(EnumObject.class)
                .withType(EnumObjectExtension.class)
                .builder()
                .build();

        assertThat(document).isNotNull();
        assertThat(document.getDefinitions()).isNotEmpty();
    }
}
