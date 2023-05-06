package com.spdb.sre.model;

public enum WsRequestType {
    ExecutePowershell(1);

    private int value;

    private WsRequestType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
