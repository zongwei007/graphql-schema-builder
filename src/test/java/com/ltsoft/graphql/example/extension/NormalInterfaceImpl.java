package com.ltsoft.graphql.example.extension;

import com.ltsoft.graphql.annotations.GraphQLType;
import com.ltsoft.graphql.example.iface.NormalInterface;

@GraphQLType
public class NormalInterfaceImpl implements NormalInterface {

    private String data;

    private String info;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
