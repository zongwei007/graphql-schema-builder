package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.example.enumeration.EnumObject;
import com.ltsoft.graphql.example.enumeration.EnumUnsupport;
import com.ltsoft.graphql.example.extension.EnumObjectExtension;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumTypeResolverTest extends BasicTypeResolverTest {

    @Test
    public void isSupport() {
        EnumTypeResolver resolver = new EnumTypeResolver();

        assertThat(resolver.isSupport(EnumObject.class)).isTrue();
        assertThat(resolver.isSupport(EnumObjectExtension.class)).isTrue();
        assertThat(resolver.isSupport(EnumUnsupport.class)).isFalse();
    }

    @Test
    public void resolve() {
        EnumTypeResolver resolver = new EnumTypeResolver();

        assertThat(printDefinition(EnumObject.class, resolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/enumeration/EnumObject.graphql"));
    }
}
