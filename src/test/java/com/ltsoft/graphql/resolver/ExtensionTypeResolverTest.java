package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.example.extension.*;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtensionTypeResolverTest extends BasicTypeResolverTest {

    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    public void isSupport() {
        assertThat(new InputObjectTypeResolver().isSupport(MutationInputObjectExtension.class)).isTrue();
        assertThat(new InterfaceTypeResolver(instanceFactory).isSupport(NormalInterfaceExtension.class)).isTrue();
        assertThat(new ObjectTypeResolver(instanceFactory, Collections.emptyList()).isSupport(NormalInterfaceImplExtension.class)).isTrue();
        assertThat(new EnumTypeResolver().isSupport(EnumObjectExtension.class)).isTrue();
        assertThat(new UnionTypeResolver(instanceFactory).isSupport(UnionObjectWithExtension.class)).isTrue();
    }

    @Test
    public void resolveInput() {
        InputObjectTypeResolver inputResolver = new InputObjectTypeResolver();
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(MutationInputObjectExtension.class, inputResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/extension/MutationInputObjectExtension.graphql"));
    }

    @Test
    public void testInterface() {
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(NormalInterfaceExtension.class, interfaceResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/extension/NormalInterfaceExtension.graphql"));
    }

    @Test
    public void testObject() {
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(NormalInterfaceImplExtension.class, interfaceResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/extension/NormalInterfaceImplExtension.graphql"));
    }

    @Test
    public void testEnum() {
        EnumTypeResolver enumResolver = new EnumTypeResolver();

        assertThat(printDefinition(EnumObjectExtension.class, enumResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/extension/EnumObjectExtension.graphql"));
    }

    @Test
    public void testUnion() {
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();
        UnionTypeResolver unionResolver = new UnionTypeResolver(instanceFactory);

        assertThat(printDefinition(UnionObjectWithExtension.class, interfaceResolver, objectResolver, scalarResolver, unionResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/extension/UnionObjectWithExtension.graphql"));
    }
}
