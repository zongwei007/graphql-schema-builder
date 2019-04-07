package com.ltsoft.graphql.example.custom;

public class CustomType<T> {

    private T info;

    public T getInfo() {
        return info;
    }

    public void setInfo(T info) {
        this.info = info;
    }
}
