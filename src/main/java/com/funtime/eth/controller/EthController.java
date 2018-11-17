package com.funtime.eth.controller;

import com.alibaba.fastjson.JSON;
import com.funtime.eth.model.*;
import com.funtime.eth.service.BlockchainService;
import com.funtime.eth.service.TokenService;
import com.funtime.eth.utils.OkHttpUtil;
import io.swagger.annotations.ApiOperation;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class EthController {
    private static final Logger logger = LoggerFactory.getLogger(EthController.class);
    @Autowired
    private Web3j web3j;

    @Value("${config.transaction_callback}")
    public String TRANSACTION_CALLBACK;

    private final BlockchainService service;
    private final TokenService tokenChainService;

    public EthController(BlockchainService service, TokenService tokenChainService) {
        this.service = service;
        this.tokenChainService = tokenChainService;
    }

    @PostMapping("/process")
    public EthTransaction execute(@RequestBody EthTransaction transaction) throws IOException {
        logger.info("REST process() REQ-->" + JSON.toJSONString(transaction));
        return service.process(transaction);
    }

    /**
     * Create ETH Adderss
     *
     * @return
     * @throws IOException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws CipherException
     */
    @ApiOperation(value = "Create ETH Adderss", notes = "Create ETH Adderss")
    @RequestMapping(value = {"/createWallet"}, method = RequestMethod.POST)
    public Credentials createWallet() {
        logger.info("REST createWallet() REQ-->");
        Credentials wallet = null;
        try {
            wallet = service.createWallet();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Private key: 0x" + Hex.toHexString(wallet.getEcKeyPair().getPrivateKey().toByteArray()));
        return wallet;
    }

    /**
     * Load ETH Adderss
     *
     * @param wallet
     * @return
     * @throws IOException
     * @throws CipherException
     */
    @ApiOperation(value = "Load ETH Adderss", notes = "Load ETH Adderss")
    @RequestMapping(value = "loadWallet", method = RequestMethod.POST)
    public Credentials loadWallet(@RequestBody WalletQueryRequest wallet) throws IOException, CipherException {
        logger.info("REST loadWallet() REQ-->" + JSON.toJSONString(wallet));
        return service.loadWallet(wallet.getWalleFilePath(), wallet.getPassWord());
    }


    /**
     * Get Eth Banlance
     *
     * @param address
     * @return wei
     */
    @ApiOperation(value = "Get Eth Banlance,Return WEI", notes = "Get Eth Banlance,Return WEI")
    @RequestMapping(value = {"/getEthBanlanceWei/{address}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public BigInteger getBalanceWei(@PathVariable("address") String address) {
        logger.info("REST getBalanceWei() REQ-->" + address);
        return service.getBalanceWei(address);
    }

    /**
     * Get Eth Banlance
     *
     * @param address
     * @return eth
     */
    @ApiOperation(value = "Get Eth Banlance,Return ETH", notes = "Get Eth Banlance,Return ETH")
    @RequestMapping(value = {"/getEthBanlance/{address}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public BigDecimal getBalanceEth(@PathVariable("address") String address) {
        logger.info("REST getBalanceEth() REQ-->" + address);
        return service.getBalanceEth(address);
    }

    /**
     * Get Transaction Receipt
     *
     * @param transactionHash
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ApiOperation(value = "Get Transaction Receipt", notes = "Get Transaction Receipt")
    @RequestMapping(value = {"/getTransactionReceipt/{transactionHash}"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public TransactionReceiptResponse getTransactionReceipt(@PathVariable("transactionHash") String transactionHash) throws IOException, InterruptedException, ExecutionException {
        logger.info("REST getTransactionReceipt() REQ-->" + transactionHash);
        TransactionReceipt transactionReceipt = null;
        TransactionReceiptResponse transactionReceiptResponse = null;
        try {
            org.web3j.protocol.core.methods.response.EthTransaction ethTransaction = tokenChainService.getTransaction(transactionHash);
            transactionReceipt = service.getTransactionReceipt(transactionHash);
            transactionReceiptResponse = new TransactionReceiptResponse();
            if (ethTransaction.getResult() != null && transactionReceipt != null) {
                BigInteger gasPrice = ethTransaction.getResult().getGasPrice();
                BigInteger time = tokenChainService.getBlockEthBlock(ethTransaction.getResult().getBlockNumber().intValue()).getBlock().getTimestamp();
                BigInteger defaultGas = new BigInteger("21000");
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

    @ApiOperation(value = "Get Web3 Client Version", notes = "Get Web3 Client Version")
    @RequestMapping(value = {"/getWeb3ClientVersion"}, method = {RequestMethod.GET}, produces = {"application/json"})
    public String getWeb3ClientVersion() throws IOException {
        Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
        return web3ClientVersion.getWeb3ClientVersion();
    }

    /**
     * ETH Transaction
     *
     * @param transaction
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "ETH Transaction", notes = "ETH Transaction")
    @RequestMapping(value = "transaction", method = RequestMethod.POST)
    public TokenSendTransaction transto(@RequestBody TokenTransaction transaction) throws Exception {
        logger.info("REST transaction() REQ-->" + JSON.toJSONString(transaction));
        TokenSendTransaction tokenEthSendTransaction = null;
        try {
            tokenEthSendTransaction = new TokenSendTransaction();
            EthSendTransaction ethSendTransaction = service.transto(transaction);
            tokenEthSendTransaction.setResult(ethSendTransaction.getResult());
            tokenEthSendTransaction.setError(ethSendTransaction.getError());
            tokenEthSendTransaction.setId(ethSendTransaction.getId());
            tokenEthSendTransaction.setJsonrpc(ethSendTransaction.getJsonrpc());
            // call back
            if (transaction.getBusinesCode() != null) {
                logger.info("transaction.getBusinesCode():" + transaction.getBusinesCode());
                tokenEthSendTransaction.setBusinesCode(transaction.getBusinesCode());
                OkHttpUtil.postJsonParams(TRANSACTION_CALLBACK, JSON.toJSONString(tokenEthSendTransaction));
                logger.info("transaction callback end!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokenEthSendTransaction;
    }
}