package com.funtime.eth.model;

import lombok.Data;

@Data
public class WalletQueryRequest {
    private String walleFilePath;
    private String  passWord;
}
