package com.funtime.eth.controller;

import com.alibaba.fastjson.JSON;
import com.funtime.eth.model.*;
import com.funtime.eth.service.TokenService;
import com.funtime.eth.utils.ConvertUtil;
import com.funtime.eth.utils.JsonUtil;
import com.funtime.eth.utils.OkHttpUtil;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    @Value("${config.third_party_api}")
    public String THIRD_PARTY_API;

    @Value("${config.third_party_price_api}")
    public String THIRD_PARTY_PRICE_API;

    @Value("${config.transaction_callback}")
    public String TRANSACTION_CALLBACK;

    private final TokenService service;

    private final Web3j web3j;

    private static String ETH_TYPE = "m/44'/60'/0'/0/0";

    public TokenController(TokenService service, Web3j web3j) {
        this.service = service;
        this.web3j = web3j;
    }

    @ApiOperation(value = "ETH账号生成", notes = "ETH账号生成,包含助记词")
    @RequestMapping(value = {"/token/createWallet"}, method = RequestMethod.POST)
    public EthHDWallet createWallet(String password) {
        logger.info("REST createWallet() REQ-->" + password);
        return service.generateMnemonic(ETH_TYPE, password);
    }

    /**
     * 查询代币余额
     */
    @ApiOperation(value = "查询代币余额", notes = "查询代币余额")
    @RequestMapping(value = {"/token/getBanlance/{fromAddress}/{contractAddress}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public String getTokenBalance(@PathVariable("fromAddress") String fromAddress, @PathVariable("contractAddress") String contractAddress) {
        logger.info("REST getTokenBalance() REQ-->" + fromAddress);
        return service.getBalance(fromAddress, contractAddress);
    }

    /**
     * 查询代币名称
     *
     * @param contractAddress
     * @return
     */
    @ApiOperation(value = "查询代币名称", notes = "查询代币名称")
    @RequestMapping(value = {"/token/getTokenName/{contractAddress}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public String getTokenName(@PathVariable("contractAddress") String contractAddress) {
        logger.info("REST getTokenName() REQ-->" + contractAddress);
        return service.getTokenName(contractAddress);
    }

    /**
     * 查询代币符号
     *
     * @param contractAddress
     * @return
     */
    @ApiOperation(value = "查询代币符号", notes = "查询代币符号")
    @RequestMapping(value = {"/token/getTokenSymbol/{contractAddress}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public String getTokenSymbol(@PathVariable("contractAddress") String contractAddress) {
        logger.info("REST getTokenSymbol() REQ-->" + contractAddress);
        return service.getTokenSymbol(contractAddress);
    }

    /**
     * 查询代币精度
     *
     * @param contractAddress
     * @return
     */
    @ApiOperation(value = "查询代币精度", notes = "查询代币精度")
    @RequestMapping(value = {"/token/getTokenDecimals/{contractAddress}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public int getTokenDecimals(@PathVariable("contractAddress") String contractAddress) {
        logger.info("REST getTokenDecimals() REQ-->" + contractAddress);
        return service.getTokenDecimals(contractAddress);
    }

    /**
     * 查询代币发行总量
     *
     * @param contractAddress
     * @return
     */
    @ApiOperation(value = "查询代币发行总量", notes = "查询代币发行总量")
    @RequestMapping(value = {"/token/getTokenTotalSupply/{contractAddress}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public BigInteger getTokenTotalSupply(@PathVariable("contractAddress") String contractAddress) {
        logger.info("REST getTokenTotalSupply() REQ-->" + contractAddress);
        return service.getTokenTotalSupply(contractAddress);
    }

    /**
     * 代币转账
     *
     * @param transaction
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "代币转账", notes = "代币转账")
    @RequestMapping(value = "/token/transaction", method = RequestMethod.POST)
    public TokenSendTransaction transto(@RequestBody TokenTransaction transaction) {
        logger.info("REST token/transaction() REQ-->" + JSON.toJSONString(transaction));
        TokenSendTransaction tokenEthSendTransaction = null;
        try {
            tokenEthSendTransaction = new TokenSendTransaction();
            EthSendTransaction ethSendTransaction = service.tokenTransaction(transaction);
            tokenEthSendTransaction.setResult(ethSendTransaction.getResult());
            tokenEthSendTransaction.setError(ethSendTransaction.getError());
            tokenEthSendTransaction.setId(ethSendTransaction.getId());
            tokenEthSendTransaction.setJsonrpc(ethSendTransaction.getJsonrpc());
            // call back
            if (transaction.getBusinesCode() != null) {
                logger.info("transaction.getBusinesCode():" + transaction.getBusinesCode());
                tokenEthSendTransaction.setBusinesCode(transaction.getBusinesCode());
                OkHttpUtil.postJsonParams(TRANSACTION_CALLBACK, JSON.toJSONString(tokenEthSendTransaction));
                logger.info("transaction callback end!" + JSON.toJSONString(tokenEthSendTransaction));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokenEthSendTransaction;
    }

    @ApiOperation(value = "交易记录列表", notes = "交易记录列表")
    @RequestMapping(value = {"/token/getAccountTransactions/{address}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public String getAccountTransactions(@PathVariable("address") String address) {
        logger.info("REST token/getAccountTransactions() REQ-->" + address);
        String result = null;
        try {
            result = OkHttpUtil.get(THIRD_PARTY_API.replace("{}", address), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @ApiOperation(value = "Gas 估算", notes = "Gas 估算")
    @RequestMapping(value = {"/token/getEthEstimateGas/{wei}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public TokenEstimateGas getEthEstimateGas(@PathVariable("wei") BigDecimal wei) {
        logger.info("REST token/getEthEstimateGas() REQ-->" + wei);
        TokenEstimateGas tokenEthEstimateGas = null;
        try {
            BigInteger gasPrice = Convert.toWei(wei, Convert.Unit.GWEI).toBigInteger();
            BigInteger gasLimit = BigInteger.valueOf(60000);
            BigInteger bgasUsed = gasLimit.multiply(gasPrice);
            BigDecimal gasUsed = Convert.fromWei(bgasUsed.toString(), Convert.Unit.ETHER);
            tokenEthEstimateGas = new TokenEstimateGas();
            tokenEthEstimateGas.setGasLimit(gasLimit);
            tokenEthEstimateGas.setWei(wei);
            tokenEthEstimateGas.setGasUse(gasUsed);

            // ETH->USD,CNY
            String result = OkHttpUtil.get(THIRD_PARTY_PRICE_API, null);
            Map<String, Object> map = JsonUtil.convertJsonStrToMap(result);
            tokenEthEstimateGas.setRmb(gasUsed.multiply(ConvertUtil.getBigDecimal(map.get("CNY"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokenEthEstimateGas;
    }

    /**
     * 交易状态查询
     *
     * @param transactionHash
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ApiOperation(value = "交易状态查询", notes = "交易状态查询")
    @RequestMapping(value = {"/token/getTransaction/{transactionHash}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public TransactionReceiptResponse getTransaction(@PathVariable("transactionHash") String transactionHash) {
        logger.info("REST token/getTransaction() REQ-->" + transactionHash);
        TransactionReceipt transactionReceipt = null;
        TransactionReceiptResponse transactionReceiptResponse = null;
        try {
            EthTransaction ethTransaction = service.getTransaction(transactionHash);
            transactionReceipt = service.getTransactionReceipt(transactionHash);
            transactionReceiptResponse = new TransactionReceiptResponse();
            if (ethTransaction.getResult() != null && transactionReceipt != null) {
                BigInteger gasPrice = ethTransaction.getResult().getGasPrice();
                //            EthBlock.Block ethBlock = web3j.ethGetBlockByHash(ethTransaction.getResult().getBlockHash(), false).send().getBlock();
                BigInteger time = service.getBlockEthBlock(ethTransaction.getResult().getBlockNumber().intValue()).getBlock().getTimestamp();
                BigInteger defaultGas = new BigInteger("60000");
                BigInteger gasLimit = ethTransaction.getResult().getGas().compareTo(defaultGas) == 1 ? defaultGas : ethTransaction.getResult().getGas();
                BigInteger bgasUsed = gasLimit.multiply(gasPrice);
                BigDecimal gasUsed = Convert.fromWei(bgasUsed.toString(), Convert.Unit.ETHER);
                BigDecimal amount = Convert.fromWei(ethTransaction.getTransaction().get().getValue().toString(), Convert.Unit.ETHER);

                transactionReceiptResponse.setAmount(String.valueOf(amount));
                transactionReceiptResponse.setBlockHash(ethTransaction.getResult().getBlockHash());
                transactionReceiptResponse.setBlockNumber(ethTransaction.getResult().getBlockNumber().toString());
                transactionReceiptResponse.setContractAddress(transactionReceipt.getContractAddress());
                transactionReceiptResponse.setFrom(ethTransaction.getResult().getFrom());
                transactionReceiptResponse.setGasPrice(gasPrice.toString());
                transactionReceiptResponse.setGasUsed(String.valueOf(gasUsed));
                transactionReceiptResponse.setStatus(transactionReceipt.getStatus());
                transactionReceiptResponse.setTo(ethTransaction.getResult().getTo());
                transactionReceiptResponse.setTransactionHash(ethTransaction.getTransaction().get().getHash());
                transactionReceiptResponse.setTransactionIndex(ethTransaction.getResult().getTransactionIndex().toString());
                transactionReceiptResponse.setDateTime(String.valueOf(time));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactionReceipt == null ? null : transactionReceiptResponse;
    }
}