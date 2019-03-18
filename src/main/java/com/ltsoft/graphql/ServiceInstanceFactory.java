package com.ltsoft.graphql;

public interface ServiceInstanceFactory {

    <T> T provide(Class<T> cls);

}
