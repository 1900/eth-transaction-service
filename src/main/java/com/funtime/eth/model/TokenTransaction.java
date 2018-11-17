package com.funtime.eth.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TokenTransaction {
    private String walleFilePath;
    private String fromAddress;
    private String passWord;
    private String toAddress;
    private String contractAddress;
    private BigDecimal amount;
    private String privateKey;
    private int decimals;
    private BigDecimal wei;
    private String businesCode;
}
