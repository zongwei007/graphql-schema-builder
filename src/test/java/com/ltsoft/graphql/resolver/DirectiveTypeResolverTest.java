package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.annotations.GraphQLNotNull;
import com.ltsoft.graphql.example.directive.NormalDirective;
import com.ltsoft.graphql.example.directive.NormalDirectiveExample;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectiveTypeResolverTest extends BasicTypeResolverTest {

    private DefaultInstanceFactory instanceFactory = new DefaultInstanceFactory();

    @Test
    public void isSupport() {
        DirectiveTypeResolver resolver = new DirectiveTypeResolver();

        assertThat(resolver.isSupport(NormalDirective.class)).isTrue();
        assertThat(resolver.isSupport(GraphQLNotNull.class)).isFalse();
    }

    @Test
    public void resolve() {
        DirectiveTypeResolver directiveResolver = new DirectiveTypeResolver();
        InterfaceTypeResolver interfaceResolver = new InterfaceTypeResolver(instanceFactory);
        ScalarTypeResolver scalarResolver = new ScalarTypeResolver();

        assertThat(printDefinition(NormalDirectiveExample.class, directiveResolver, interfaceResolver, scalarResolver))
                .isEqualToIgnoringWhitespace(readSchemaExample("/example/directive/NormalDirective.graphql"));
    }
}
