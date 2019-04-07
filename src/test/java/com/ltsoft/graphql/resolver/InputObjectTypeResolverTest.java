package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.example.input.MutationInputObject;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class InputObjectTypeResolverTest extends BasicTypeResolverTest {

    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    public void isSupport() {
        InputObjectTypeResolver inputResolver = new InputObjectTypeResolver();

        assertThat(inputResolver.isSupport(MutationInputObject.class)).isTrue();
    }

    @Test
    public void resolve() {
        InputObjectTypeResolver inputResolver = new InputObjectTypeResolver();
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory, Collections.emptyList());
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(MutationInputObject.class, inputResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/input/MutationInputObject.graphql"));
    }
}
