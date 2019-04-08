package com.ltsoft.graphql.example.iface;

import com.ltsoft.graphql.annotations.GraphQLType;

@GraphQLType
public class InterfaceImpl extends AbstractInterface {

    private String info;

    private Integer impl;

    @Override
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Integer getImpl() {
        return impl;
    }

    public void setImpl(Integer impl) {
        this.impl = impl;
    }
}
