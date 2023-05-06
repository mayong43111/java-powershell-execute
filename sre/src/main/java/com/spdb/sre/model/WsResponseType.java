package com.spdb.sre.model;

public enum WsResponseType {

    ExecutePowershellOutput(1),
    ExecutePowershellCompleted(2);

    private int value;

    private WsResponseType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
