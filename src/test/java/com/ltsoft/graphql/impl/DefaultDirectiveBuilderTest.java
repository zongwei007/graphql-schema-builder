package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.annotations.GraphQLDeprecate;
import com.ltsoft.graphql.example.directive.NormalDirective;
import com.ltsoft.graphql.example.directive.NormalDirectiveExample;
import graphql.language.Directive;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultDirectiveBuilderTest {

    @Test
    public void test() {
        DefaultDirectiveBuilder builder = new DefaultDirectiveBuilder();

        assertThat(builder.isSupport(GraphQLDeprecate.class)).isTrue();
        assertThat(builder.isSupport(NormalDirective.class)).isTrue();

        Directive result = builder.builder(NormalDirectiveExample.class.getAnnotation(NormalDirective.class))
                .build();

        assertThat(result.getArguments()).hasSize(2);
    }

}
