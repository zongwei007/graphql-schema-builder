package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.example.MutationService;
import com.ltsoft.graphql.example.NotConstructor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class DefaultServiceInstanceFactoryTest {

    @Test
    public void provide() {

        DefaultServiceInstanceFactory instanceFactory = new DefaultServiceInstanceFactory();

        MutationService service = instanceFactory.provide(MutationService.class);

        assertThat(service).isInstanceOf(MutationService.class);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> instanceFactory.provide(NotConstructor.class))
                .withCauseInstanceOf(InstantiationException.class);

    }
}
