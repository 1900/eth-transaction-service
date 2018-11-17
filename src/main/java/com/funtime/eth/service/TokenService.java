package com.funtime.eth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funtime.eth.model.EthHDWallet;
import com.funtime.eth.model.TokenTransaction;
import com.funtime.eth.utils.Constants;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.*;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final Web3j web3j;
//    private final Admin admin;

    private static SecureRandom secureRandom = new SecureRandom();

    public TokenService(Web3j web3j) {
        this.web3j = web3j;
//        this.admin = admin;
    }

    /**
     * 查询代币余额
     *
     * @param fromAddress
     * @param contractAddress
     * @return
     */
    public String getBalance(String fromAddress, String contractAddress) {
        String methodName = "balanceOf";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address address = new Address(fromAddress);
        inputParameters.add(address);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);

        EthCall ethCall;
        BigInteger balanceValue = BigInteger.ZERO;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            balanceValue = (BigInteger) results.get(0).getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String decimal = toDecimal(18, balanceValue);
        logger.info(" balance " + balanceValue);
        return decimal;
    }

    public static String toDecimal(int decimal, BigInteger integer) {
//		String substring = str.substring(str.length() - decimal);
        StringBuffer sbf = new StringBuffer("1");
        for (int i = 0; i < decimal; i++) {
            sbf.append("0");
        }
        String balance = new BigDecimal(integer).divide(new BigDecimal(sbf.toString()), 18, BigDecimal.ROUND_DOWN).toPlainString();
        return balance;
    }

    /**
     * 查询代币名称
     *
     * @param contractAddress
     * @return
     */
    public String getTokenName(String contractAddress) {
        String methodName = "name";
        String name = null;
        String fromAddr = Constants.EMPTY_ADDRESS;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            name = results.get(0).getValue().toString();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 查询代币符号
     *
     * @param contractAddress
     * @return
     */
    public String getTokenSymbol(String contractAddress) {
        String methodName = "symbol";
        String symbol = null;
        String fromAddr = Constants.EMPTY_ADDRESS;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            symbol = results.get(0).getValue().toString();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return symbol;
    }

    /**
     * 查询代币精度
     *
     * @param contractAddress
     * @return
     */
    public int getTokenDecimals(String contractAddress) {
        String methodName = "decimals";
        String fromAddr = Constants.EMPTY_ADDRESS;
        int decimal = 0;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Uint8> typeReference = new TypeReference<Uint8>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            decimal = Integer.parseInt(results.get(0).getValue().toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return decimal;
    }

    /**
     * 查询代币发行总量
     *
     * @param contractAddress
     * @return
     */
    public BigInteger getTokenTotalSupply(String contractAddress) {
        String methodName = "totalSupply";
        String fromAddr = Constants.EMPTY_ADDRESS;
        BigInteger totalSupply = BigInteger.ZERO;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            totalSupply = (BigInteger) results.get(0).getValue();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return totalSupply;
    }

    /**
     * 代币转账
     *
     * @param transaction
     * @return
     */
//    public String sendTokenTransaction(TokenTransaction transaction) {
//        String txHash = null;
//
//        try {
//            PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(
//                    transaction.getFromAddress(), transaction.getPassWord(), BigInteger.valueOf(10)).send();
//            if (personalUnlockAccount.accountUnlocked()) {
//                String methodName = "transfer";
//                List<Type> inputParameters = new ArrayList<>();
//                List<TypeReference<?>> outputParameters = new ArrayList<>();
//
//                Address tAddress = new Address(transaction.getToAddress());
//
//                Uint256 value = new Uint256(BigInteger.valueOf(transaction.getAmount().longValue()));
//                inputParameters.add(tAddress);
//                inputParameters.add(value);
//
//                TypeReference<Bool> typeReference = new TypeReference<Bool>() {
//                };
//                outputParameters.add(typeReference);
//
//                Function function = new Function(methodName, inputParameters, outputParameters);
//
//                String data = FunctionEncoder.encode(function);
//
//                EthGetTransactionCount ethGetTransactionCount = web3j
//                        .ethGetTransactionCount(transaction.getFromAddress(), DefaultBlockParameterName.PENDING).sendAsync().get();
//                BigInteger nonce = ethGetTransactionCount.getTransactionCount();
//                BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(5), Convert.Unit.GWEI).toBigInteger();
//
//                Transaction tr = Transaction.createFunctionCallTransaction(transaction.getFromAddress(), nonce, gasPrice,
//                        BigInteger.valueOf(60000), transaction.getContractAddress(), data);
//
//                EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(tr).sendAsync().get();
//                txHash = ethSendTransaction.getTransactionHash();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return txHash;
//    }

    /**
     * 交易,转账
     *
     * @param transaction
     */
    public EthSendTransaction tokenTransaction(TokenTransaction transaction) {
        transaction.setDecimals(18);
        BigInteger nonce;
        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(transaction.getFromAddress(), DefaultBlockParameterName.PENDING).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ethGetTransactionCount == null) ;
        nonce = ethGetTransactionCount.getTransactionCount();
        System.out.println("nonce " + nonce);
        BigInteger gasPrice = Convert.toWei(transaction.getWei(), Convert.Unit.GWEI).toBigInteger();
        BigInteger gasLimit = BigInteger.valueOf(60000);
        BigInteger value = BigInteger.ZERO;
        //token转账参数
        String methodName = "transfer";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address tAddress = new Address(transaction.getToAddress());
        Uint256 tokenValue = new Uint256(transaction.getAmount().multiply(BigDecimal.TEN.pow(transaction.getDecimals())).toBigInteger());
        inputParameters.add(tAddress);
        inputParameters.add(tokenValue);
        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);

        byte chainId = ChainId.NONE;
        String signedData;
        EthSendTransaction ethSendTransaction = null;
        try {
            signedData = signTransaction(nonce, gasPrice, gasLimit, transaction.getContractAddress(), value, data, chainId, transaction.getPrivateKey());
            if (signedData != null) {
                ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
                System.out.println(ethSendTransaction.getTransactionHash());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ethSendTransaction;
    }

    /**
     * 签名
     */
    public String signTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
                                  BigInteger value, String data, byte chainId, String privateKey) throws IOException {
        byte[] signedMessage;
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value,
                data);

        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);

        if (chainId > ChainId.NONE) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        String hexValue = Numeric.toHexString(signedMessage);
        return hexValue;
    }

    public EthHDWallet generateMnemonic(String path, String password) {
        if (!path.startsWith("m") && !path.startsWith("M")) {
            //参数非法
            return null;
        }
        String[] pathArray = path.split("/");
        if (pathArray.length <= 1) {
            //内容不对
            return null;
        }

        if (password.length() < 6) {
            //密码过短
            return null;
        }

        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, passphrase, creationTimeSeconds);
        return createEthWallet(ds, pathArray, password);
    }

    private EthHDWallet importMnemonic(String path, List<String> list, String password) {
        if (!path.startsWith("m") && !path.startsWith("M")) {
            //参数非法
            return null;
        }
        String[] pathArray = path.split("/");
        if (pathArray.length <= 1) {
            //内容不对
            return null;
        }
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed ds = new DeterministicSeed(list, null, passphrase, creationTimeSeconds);

        return createEthWallet(ds, pathArray, password);
    }

    private EthHDWallet createEthWallet(DeterministicSeed ds, String[] pathArray, String password) {
        //根私钥
        byte[] seedBytes = ds.getSeedBytes();
        logger.info("根私钥 " + Arrays.toString(seedBytes));
        //助记词
        List<String> mnemonic = ds.getMnemonicCode();
        logger.info("助记词 " + Arrays.toString(mnemonic.toArray()));

        try {
            //助记词种子
            byte[] mnemonicSeedBytes = MnemonicCode.INSTANCE.toEntropy(mnemonic);
            logger.info("助记词种子 " + Arrays.toString(mnemonicSeedBytes));
            ECKeyPair mnemonicKeyPair = ECKeyPair.create(mnemonicSeedBytes);
            WalletFile walletFile = Wallet.createLight(password, mnemonicKeyPair);
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            //存这个keystore 用完后删除
            String jsonStr = objectMapper.writeValueAsString(walletFile);
            logger.info("mnemonic keystore " + jsonStr);
            //验证
            WalletFile checkWalletFile = objectMapper.readValue(jsonStr, WalletFile.class);
            ECKeyPair ecKeyPair = Wallet.decrypt(password, checkWalletFile);
            byte[] checkMnemonicSeedBytes = Numeric.hexStringToByteArray(ecKeyPair.getPrivateKey().toString(16));
            logger.info("验证助记词种子 "
                    + Arrays.toString(checkMnemonicSeedBytes));
            List<String> checkMnemonic = MnemonicCode.INSTANCE.toMnemonic(checkMnemonicSeedBytes);
            logger.info("验证助记词 " + Arrays.toString(checkMnemonic.toArray()));

        } catch (MnemonicException.MnemonicLengthException | MnemonicException.MnemonicWordException | MnemonicException.MnemonicChecksumException | CipherException | IOException e) {
            e.printStackTrace();
        }

        if (seedBytes == null)
            return null;
        DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        for (int i = 1; i < pathArray.length; i++) {
            ChildNumber childNumber;
            if (pathArray[i].endsWith("'")) {
                int number = Integer.parseInt(pathArray[i].substring(0,
                        pathArray[i].length() - 1));
                childNumber = new ChildNumber(number, true);
            } else {
                int number = Integer.parseInt(pathArray[i]);
                childNumber = new ChildNumber(number, false);
            }
            dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber);
        }
        logger.info("path " + dkKey.getPathAsString());

        ECKeyPair keyPair = ECKeyPair.create(dkKey.getPrivKeyBytes());
        logger.info("eth privateKey " + keyPair.getPrivateKey().toString(16));
        logger.info("eth publicKey " + keyPair.getPublicKey().toString(16));

        EthHDWallet ethHDWallet = null;
        try {
            WalletFile walletFile = Wallet.createLight(password, keyPair);
            logger.info("eth address " + "0x" + walletFile.getAddress());
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            //存
            String jsonStr = objectMapper.writeValueAsString(walletFile);
            logger.info("eth keystore " + jsonStr);

            ethHDWallet = new EthHDWallet("0x" + keyPair.getPrivateKey().toString(16),
                    keyPair.getPublicKey().toString(16),
                    mnemonic, dkKey.getPathAsString(),
                    "0x" + walletFile.getAddress(), jsonStr);
        } catch (CipherException | JsonProcessingException e) {
            e.printStackTrace();
        }

        return ethHDWallet;
    }

    /**
     * 获取普通交易的gas上限
     *
     * @param transaction 交易对象
     * @return gas 上限
     */
    private BigInteger getTransactionGasLimit(Transaction transaction) {
        BigInteger gasLimit = BigInteger.ZERO;
        try {
            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            gasLimit = ethEstimateGas.getAmountUsed();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gasLimit;
    }

    /**
     * 交易状态查询
     *
     * @param transactionHash
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public EthTransaction getTransaction(String transactionHash) throws InterruptedException, ExecutionException {
        EthTransaction ethGetTransaction = web3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        return ethGetTransaction;
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
     * 获得ethblock
     *
     * @param blockNumber 根据区块编号
     * @return
     * @throws IOException
     */
    public EthBlock getBlockEthBlock(Integer blockNumber) throws IOException {
        DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(blockNumber);
        Request<?, EthBlock> request = web3j.ethGetBlockByNumber(defaultBlockParameter, true);
        EthBlock ethBlock = request.send();
        return ethBlock;
    }
}
