package com.ltsoft.graphql.impl;

import graphql.schema.DataFetchingEnvironment;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQLDataFetchingEnvironmentProviderTest {

    @Test
    public void apply() {
        DataFetchingEnvironment environment = Mockito.mock(DataFetchingEnvironment.class);
        GraphQLDataFetchingEnvironmentProvider provider = new GraphQLDataFetchingEnvironmentProvider();

        assertThat(provider.apply(environment)).isEqualTo(environment);
    }
}
