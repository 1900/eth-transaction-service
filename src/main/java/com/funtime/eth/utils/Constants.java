package com.funtime.eth.utils;

import java.math.BigInteger;

public class Constants {

    public static final String A_USER_ADDRESS = "0xbe782DDC49A055680f825aEce6D60e276F281659";

    public static final String B_USER_ADDRESS = "0xD054C0294313478eCbAw08DCd5Ac7aAFA00822b6";


    public static final String A_USER_WALLET_PATH = "UTC--2018-08-20T02-45-23.28000000Z--be782ddc49a015680f821aece6d60e276f281659.json";

    public static final String B_USER_WALLET_PATH = "UTC--2017-09-12T10-14-23.678000000Z--39c633e6fa59b7fdc4025ecca55ca9e91d544e8a.json";

    //erc20 token address
    public static final String TOKEN_CONTRACT_ADDRESS = "";

    public static final String EMPTY_ADDRESS = "0x0000000000000000000000000000000000000000";

    public static final String DEFAULT_PASSWORD = "123456";

    public static final String TXT = ".txt";

    // see https://www.reddit.com/r/ethereum/comments/5g8ia6/attention_miners_we_recommend_raising_gas_limit/
    public static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);

    // http://ethereum.stackexchange.com/questions/1832/cant-send-transaction-exceeds-block-gas-limit-or-intrinsic-gas-too-low
    public static final BigInteger GAS_LIMIT_ETHER_TX = BigInteger.valueOf(21_000);
    public static final BigInteger GAS_LIMIT_GREETER_TX = BigInteger.valueOf(500_000L);

    public static final int CONFIRMATION_ATTEMPTS = 40;
    public static final int SLEEP_DURATION = 1000;

    // file name extensions for smart contracts
    public static final String EXT_SOLIDITY = "sol";
    public static final String EXT_BINARY = "bin";
    public static final String EXT_ABI = "abi";
}