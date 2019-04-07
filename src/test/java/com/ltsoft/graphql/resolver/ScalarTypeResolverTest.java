package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.TypeProvider;
import org.junit.Test;

import java.net.InterfaceAddress;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeResolverTest extends BasicTypeResolverTest {

    @Test
    public void isSupport() {
        ScalarTypeResolver resolver = new ScalarTypeResolver();

        assertThat(resolver.isSupport(String.class)).isTrue();
        assertThat(resolver.isSupport(Long.class)).isTrue();
        assertThat(resolver.isSupport(int.class)).isTrue();
        assertThat(resolver.isSupport(OffsetDateTime.class)).isTrue();
        assertThat(resolver.isSupport(Map.class)).isTrue();
        assertThat(resolver.isSupport(InterfaceAddress.class)).isFalse();
    }

    @Test
    public void resolve() {
        ScalarTypeResolver resolver = new ScalarTypeResolver();

        assertThat(resolver.resolve(String.class, type -> null))
                .isNotNull()
                .extracting(TypeProvider::getTypeName)
                .containsOnly("String");

    }
}
