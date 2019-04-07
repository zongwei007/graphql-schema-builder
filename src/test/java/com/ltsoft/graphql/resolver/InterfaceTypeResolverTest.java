package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.example.iface.NormalInterface;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InterfaceTypeResolverTest extends BasicTypeResolverTest {

    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    public void isSupport() {
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);

        assertThat(interfaceResolver.isSupport(NormalInterface.class)).isTrue();
    }

    @Test
    public void resolve() {
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(NormalInterface.class, interfaceResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/iface/NormalInterface.graphql"));
    }
}
