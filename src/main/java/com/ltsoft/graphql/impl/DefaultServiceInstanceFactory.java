package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.ServiceInstanceFactory;

public class DefaultServiceInstanceFactory implements ServiceInstanceFactory {
    @Override
    public <T> T provide(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Can not invoke constructor of class '%s'", cls), e);
        }
    }
}
