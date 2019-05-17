package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.InstanceFactory;
import com.ltsoft.graphql.example.custom.CustomTypeResolver;
import com.ltsoft.graphql.example.custom.ObjectForTypeResolver;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomTypeResolverTest extends BasicTypeResolverTest {

    private InstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    public void test() {
        CustomTypeResolver customResolver = new CustomTypeResolver();
        ObjectTypeResolver objectResolver = new ObjectTypeResolver(instanceFactory);
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(ObjectForTypeResolver.class, customResolver, objectResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/custom/ObjectForTypeResolver.graphql"));
    }

}
