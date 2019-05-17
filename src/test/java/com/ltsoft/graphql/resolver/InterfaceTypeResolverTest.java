package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.example.iface.InterfaceImpl;
import com.ltsoft.graphql.example.iface.NormalInterface;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import java.util.Collections;

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

    @Test
    public void resolveClass() {
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory);
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(InterfaceImpl.class, interfaceResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/iface/InterfaceImpl.graphql"));
    }
}
