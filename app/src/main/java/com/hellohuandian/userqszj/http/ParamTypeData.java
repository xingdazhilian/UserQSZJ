package com.hellohuandian.userqszj.http;

public class ParamTypeData {
    private String name;
    private String value;

    public ParamTypeData(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
