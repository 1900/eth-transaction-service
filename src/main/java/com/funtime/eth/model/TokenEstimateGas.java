package com.funtime.eth.model;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class TokenEstimateGas {
    BigDecimal wei;
    BigInteger gasLimit;
    BigDecimal gasUse;
    BigDecimal rmb;
}
