package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.InstanceFactory;

import java.lang.reflect.InvocationTargetException;

public class DefaultInstanceFactory implements InstanceFactory {
    @Override
    public <T> T provide(Class<T> cls) {
        try {
            return cls.getConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Can not invoke constructor of class '%s'", cls), e);
        }
    }
}
