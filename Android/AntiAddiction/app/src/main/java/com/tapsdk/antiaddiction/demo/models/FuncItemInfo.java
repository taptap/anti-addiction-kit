package com.tapsdk.antiaddiction.demo.models;

public class FuncItemInfo extends FuncBase{

    public String funcName;

    public FuncAction funcAction;

    public FuncItemInfo(String funcName, FuncAction funcAction) {
        this.funcName = funcName;
        this.funcAction = funcAction;
    }
}
