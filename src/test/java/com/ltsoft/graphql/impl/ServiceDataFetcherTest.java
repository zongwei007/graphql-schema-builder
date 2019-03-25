package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.ArgumentProvider;
import com.ltsoft.graphql.example.MutationInputObject;
import com.ltsoft.graphql.example.MutationService;
import graphql.schema.DataFetchingEnvironment;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceDataFetcherTest {

    @Test
    public void getFieldName() throws Exception {
        Method method = MutationService.class.getMethod("batch", List.class);
        MutationService service = mock(MutationService.class);

        ServiceDataFetcher dataFetcher = new ServiceDataFetcher(service, method, Collections.emptyList());

        assertThat(dataFetcher.getFieldName()).isEqualTo("batch");
    }

    @Test
    public void get() throws Exception {
        MutationService service = mock(MutationService.class);
        Method method = MutationService.class.getMethod("batch", List.class);
        DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);

        List<MutationInputObject> param = Collections.emptyList();
        List<ArgumentProvider<?>> argumentProviders = Collections.singletonList(environment -> param);

        when(service.batch(eq(param))).thenReturn(1);

        ServiceDataFetcher dataFetcher = new ServiceDataFetcher(service, method, argumentProviders);

        assertThat(dataFetcher.get(env)).isEqualTo(1);
    }

}
