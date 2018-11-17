package com.funtime.eth.service;

import com.funtime.eth.model.EthTransaction;
import com.funtime.eth.model.TokenTransaction;
import com.funtime.eth.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

@Service
public class BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class);

    private final Web3j web3j;

    @Value("${config.wallet_path}")
    public String WALLET_PATH;

    @Value("${config.transaction_receipt_path}")
    public String TRANSACTION_RECEIPT_PATH;

    public BlockchainService(Web3j web3j) {
        this.web3j = web3j;
    }

    public EthTransaction process(EthTransaction trx) throws IOException {

        EthAccounts accounts = web3j.ethAccounts().send();
        EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(accounts.getAccounts().get(trx.getFromId()), DefaultBlockParameterName.LATEST).send();

        org.web3j.protocol.core.methods.request.Transaction transaction = org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(
                accounts.getAccounts().get(trx.getFromId()), transactionCount.getTransactionCount(), BigInteger.valueOf(trx.getValue()),
                BigInteger.valueOf(21000), accounts.getAccounts().get(trx.getToId()), BigInteger.valueOf(trx.getValue()));

        EthSendTransaction response = web3j.ethSendTransaction(transaction).send();

        if (response.getError() != null) {
            trx.setAccepted(false);
            logger.info("Tx rejected: {}", response.getError().getMessage());
            return trx;
        }

        trx.setAccepted(true);
        String txHash = response.getTransactionHash();
        logger.info("Tx hash: {}", txHash);

        trx.setId(txHash);
        EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();

        receipt.getTransactionReceipt().ifPresent(transactionReceipt -> logger.info("Tx receipt:  {}", transactionReceipt.getCumulativeGasUsed().intValue()));

        return trx;
    }

    /**
     * Create ETH Adderss
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws CipherException
     * @throws IOException
     */
    public Credentials createWallet() throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CipherException, IOException {
        String fileName = WalletUtils.generateFullNewWalletFile(Constants.DEFAULT_PASSWORD,
                new File(WALLET_PATH));

        Credentials credentials = WalletUtils.loadCredentials(Constants.DEFAULT_PASSWORD,
                WALLET_PATH + fileName);
        logger.info("Users wallet address : " + credentials.getAddress());
        return credentials;
    }

    /**
     * Get Wallet Balance
     *
     * @param address Wallet Adderss
     * @return Balance,wei
     */
    public BigInteger getBalanceWei(String address) {
        BigInteger balance = null;
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            balance = ethGetBalance.getBalance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("address " + address + " balance " + balance + "wei");
        return balance;
    }

    /**
     * Get Wallet Balance
     *
     * @param address Wallet Adderss
     * @return Balance,eth
     */
    public BigDecimal getBalanceEth(String address) {
        BigDecimal balance = null;
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            // 默认获取到的单位是WEI  转换为ETH
            balance = Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("address " + address + " balance " + balance + "eth");
        return balance;
    }

    /**
     * 生成一个普通交易对象
     *
     * @param fromAddress 放款方
     * @param toAddress   收款方
     * @param nonce       交易序号
     * @param gasPrice    gas 价格
     * @param gasLimit    gas 数量
     * @param value       金额
     * @return 交易对象
     */
    private org.web3j.protocol.core.methods.request.Transaction makeTransaction(String fromAddress, String toAddress,
                                                                                BigInteger nonce, BigInteger gasPrice,
                                                                                BigInteger gasLimit, BigInteger value) {
        org.web3j.protocol.core.methods.request.Transaction transaction;
        transaction = org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(fromAddress, nonce, gasPrice, gasLimit, toAddress, value);
        return transaction;
    }

    /**
     * todo
     *
     * @param trx
     * @return
     * @throws IOException
     */
    public EthTransaction processStatus(EthTransaction trx) throws IOException {
        EthAccounts accounts = web3j.ethAccounts().send();
        EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(accounts.getAccounts().get(trx.getFromId()), DefaultBlockParameterName.LATEST).send();
        org.web3j.protocol.core.methods.request.Transaction transaction = org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(accounts.getAccounts().get(trx.getFromId()), transactionCount.getTransactionCount(), BigInteger.valueOf(trx.getValue()), BigInteger.valueOf(21_000), accounts.getAccounts().get(trx.getToId()), BigInteger.valueOf(trx.getValue()));
        EthSendTransaction response = web3j.ethSendTransaction(transaction).send();
        if (response.getError() != null) {
            trx.setAccepted(false);
            return trx;
        }
        trx.setAccepted(true);
        String txHash = response.getTransactionHash();
        logger.info("Tx hash: {}", txHash);
        trx.setId(txHash);
        EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();
        if (receipt.getTransactionReceipt().isPresent()) {
            logger.info("Tx receipt: {}", receipt.getTransactionReceipt().get().getCumulativeGasUsed().intValue());
        }
        return trx;
    }

    /**
     * 交易状态查询
     *
     * @param transactionHash
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public TransactionReceipt getTransactionReceipt(String transactionHash) throws InterruptedException, ExecutionException {
        EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        return ethGetTransactionReceipt.getResult();
    }

    /**
     * 加载钱包
     *
     * @param walleFilePath
     * @param passWord
     * @return
     * @throws IOException
     * @throws CipherException
     */
    public Credentials loadWallet(String walleFilePath, String passWord) throws IOException, CipherException {
        Credentials credentials = WalletUtils.loadCredentials(passWord, walleFilePath);
//        String address = credentials.getAddress();
//        BigInteger publicKey = credentials.getEcKeyPair().getPublicKey();
//        BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
//
        return credentials;
    }

    /**
     * 交易,转账
     *
     * @param transaction
     * @return
     * @throws Exception
     */
    public EthSendTransaction transto(TokenTransaction transaction) throws Exception {
//        // 原始钱包
//        Credentials credentials = WalletUtils.loadCredentials(Constants.DEFAULT_PASSWORD, WALLET_PATH + Constants.A_USER_WALLET_PATH);
//        //开始发送eth到指定地址
//        String address_to = transaction.getToAddress();
//        TransactionReceipt send = Transfer.sendFunds(web3j, credentials, address_to, BigDecimal.valueOf(transaction.getAmount().intValue()), Convert.Unit.FINNEY).send();

        //设置需要的矿工费
        BigInteger gasPrice = Convert.toWei(transaction.getWei(), Convert.Unit.GWEI).toBigInteger();
        BigInteger gasLimit = BigInteger.valueOf(21000);

        //转账人账户地址
        String ownAddress = transaction.getFromAddress();
        //被转人账户地址
        String toAddress = transaction.getToAddress();
        //转账人私钥
        Credentials credentials = Credentials.create(transaction.getPrivateKey());
        //getNonce
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                ownAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //创建交易
        BigInteger value = Convert.toWei(transaction.getAmount(), Convert.Unit.ETHER).toBigInteger();
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce, gasPrice, gasLimit, toAddress, value);

        //签名Transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        String transactionHash = null;

        //发送交易
        EthSendTransaction ethSendTransaction =
                web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        if (ethSendTransaction.getError() != null) {
            if (ethSendTransaction.getError().getMessage().contains("known transaction")) {
                transactionHash = "0x" + ethSendTransaction.getError().getMessage().replace("known transaction:", "").trim();
            }
        } else {
            transactionHash = ethSendTransaction.getTransactionHash();
        }

        //获得transactionHash
        logger.info("transactionHash:" + transactionHash);
        ethSendTransaction.setResult(transactionHash);
        return ethSendTransaction;
    }

    /**
     * 钱包地址余额是否足够转账校验
     *
     * @param bigDecimalValue
     * @param addressBalance
     * @return
     */
    public static String checkMoney(String bigDecimalValue, String addressBalance) {
        if (new BigDecimal(addressBalance).subtract(new BigDecimal(bigDecimalValue)).compareTo(new BigDecimal("0")) <= 0) {
            return "转账金额大于钱包地址余额";
        } else {
            return "";
        }
    }
}
