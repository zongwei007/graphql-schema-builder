package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.ServiceInstanceFactory;

public class DefaultServiceInstanceFactory implements ServiceInstanceFactory {
    @Override
    public <T> T provide(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }
}
