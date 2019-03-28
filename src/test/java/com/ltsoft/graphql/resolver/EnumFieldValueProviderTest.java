package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.example.EnumObject;
import com.ltsoft.graphql.example.EnumObjectExtension;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumFieldValueProviderTest {

    @Test
    public void getValue() {
        EnumFieldValueProvider provider = new EnumFieldValueProvider(EnumObject.class);

        assertThat(provider.getValue("first")).isEqualTo(EnumObject.first);
    }

    @Test
    public void merge() {
        EnumFieldValueProvider provider = new EnumFieldValueProvider(EnumObject.class);
        EnumFieldValueProvider extProvider = new EnumFieldValueProvider(EnumObjectExtension.class);

        assertThat(provider.getValue("third")).isNull();
        assertThat(provider.merge(extProvider).getValue("no3")).isEqualTo(EnumObjectExtension.third);
    }
}
