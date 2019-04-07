package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.example.NotConstructor;
import com.ltsoft.graphql.example.object.MutationService;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class DefaultInstanceFactoryTest {

    @Test
    public void provide() {

        DefaultInstanceFactory instanceFactory = new DefaultInstanceFactory();

        MutationService service = instanceFactory.provide(MutationService.class);

        assertThat(service).isInstanceOf(MutationService.class);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> instanceFactory.provide(NotConstructor.class))
                .withCauseInstanceOf(NoSuchMethodException.class);

    }
}
