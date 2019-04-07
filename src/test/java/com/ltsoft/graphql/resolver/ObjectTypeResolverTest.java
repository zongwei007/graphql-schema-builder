package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.example.iface.NormalInterface;
import com.ltsoft.graphql.example.object.*;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectTypeResolverTest extends BasicTypeResolverTest {

    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    public void isSupport() {
        ObjectTypeResolver resolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());

        assertThat(resolver.isSupport(NormalObject.class)).isTrue();
        assertThat(resolver.isSupport(ObjectWithExtension.class)).isTrue();
        assertThat(resolver.isSupport(NormalInterface.class)).isFalse();
    }

    @Test
    public void normalObject() {
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(NormalObject.class, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/object/NormalObject.graphql"));
    }

    @Test
    public void argumentService() {
        InputObjectTypeResolver inputResolver = new InputObjectTypeResolver();
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(ArgumentService.class, inputResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/object/ArgumentService.graphql"));
    }

    @Test
    public void withGraphQLView() {
        InputObjectTypeResolver inputResolver = new InputObjectTypeResolver();
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(MutationService.class, inputResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/object/MutationService.graphql"));
    }

    @Test
    public void generic() {
        InputObjectTypeResolver inputResolver = new InputObjectTypeResolver();
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(GenericServiceImpl.class, inputResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/object/GenericServiceImpl.graphql"));
    }

    @Test
    public void fieldExtension() {
        DirectiveTypeResolver directiveResolver = new DirectiveTypeResolver();
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(ObjectWithExtension.class, directiveResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/object/ObjectWithExtension.graphql"));
    }
}
