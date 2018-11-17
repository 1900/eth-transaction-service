package com.funtime.eth.model;

import org.web3j.protocol.core.Response;

public class TokenSendTransaction extends Response<String> {

    private String businesCode;

    public TokenSendTransaction() {
    }

    public String getTransactionHash() {
        return (String) this.getResult();
    }

    public String getBusinesCode() {
        return businesCode;
    }

    public void setBusinesCode(String businesCode) {
        this.businesCode = businesCode;
    }
}

