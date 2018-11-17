package com.funtime.eth.model;

import lombok.Data;

@Data
public class EthTransaction {

    private String id;
    private int fromId;
    private int toId;
    private long value;
    private boolean accepted;

    public EthTransaction() {

    }

    public EthTransaction(int fromId, int toId, long value) {
        this.fromId = fromId;
        this.toId = toId;
        this.value = value;
    }

}