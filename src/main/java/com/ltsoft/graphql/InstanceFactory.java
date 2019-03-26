package com.ltsoft.graphql;

public interface InstanceFactory {

    <T> T provide(Class<T> cls);

}
