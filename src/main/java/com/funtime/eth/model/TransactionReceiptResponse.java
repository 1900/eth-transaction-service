package com.funtime.eth.model;

import lombok.Data;
import org.web3j.protocol.core.methods.response.Log;

import java.util.List;

@Data
public class TransactionReceiptResponse {
    private String transactionHash;
    private String transactionIndex;
    private String blockHash;
    private String blockNumber;
    private String cumulativeGasUsed;
    private String gasUsed;
    private String gasPrice;
    private String amount;
    private String contractAddress;
    private String root;
    private String status;
    private String from;
    private String to;
    private List<Log> logs;
    private String logsBloom;
    private String dateTime;
}
