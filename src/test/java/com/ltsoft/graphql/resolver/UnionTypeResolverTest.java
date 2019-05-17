package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.example.union.UnionObject;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class UnionTypeResolverTest extends BasicTypeResolverTest {

    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    public void isSupport() {
        UnionTypeResolver resolver = new UnionTypeResolver(instanceFactory);

        assertThat(resolver.isSupport(UnionObject.class)).isTrue();
    }

    @Test
    public void resolve() {
        DirectiveTypeResolver directiveResolver = new DirectiveTypeResolver();
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory);
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();
        UnionTypeResolver unionResolver = new UnionTypeResolver(instanceFactory);

        assertThat(printDefinition(UnionObject.class, directiveResolver, interfaceResolver, objectResolver, scalarResolver, unionResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/union/UnionObject.graphql"));
    }
}
