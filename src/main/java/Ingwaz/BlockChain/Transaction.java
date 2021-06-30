package Ingwaz.BlockChain;

import Ingwaz.Mining.Hash;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * A Class that represents 1 singular Transaction between Two Addresses
 * (Sender's Public Key and Receiver's Public Key).
 * <p>
 * A Transaction is created by the sender and sent to the BlockChain. The BlockChain
 * sends back a Unique TransactionID (Block# and Milliseconds since 1970). The sender signs
 * the TransactionID with his private key, which is once again sent to the blockchain, which adds
 * the Transaction to the MemPool for mining and validation (no overspending/double-spending).
 */
public class Transaction {

    byte[] extraNonce = new byte[]{0, 0};
    private String senderAddr;
    private String receiverAddr;
    private BigDecimal amount;
    private String signature;
    private String transactionID;

    public Transaction(Wallet senderAddr, BigDecimal amount, Wallet receiverAddr) {
        this.senderAddr = KeyPackager.packagePubkey(senderAddr.getPubKey());
        this.amount = amount;
        this.receiverAddr = KeyPackager.packagePubkey(receiverAddr.getPubKey());
    }

    public Transaction(String senderAddr, BigDecimal amount, String receiverAddr) {
        this.senderAddr = senderAddr;
        this.amount = amount;
        this.receiverAddr = receiverAddr;
    }

    public static void main(String[] args) {
        Signature.InitializeProvider();
        Wallet w = Wallet.createNewWallet("");
        Wallet f = Wallet.createNewWallet("");

        Transaction t = new Transaction(KeyPackager.packagePubkey(w.getPubKey()), BigDecimal.ONE,
                KeyPackager.packagePubkey(f.getPubKey()));
        t.setTXID("Sign");
        t.addSignature(
                Base64.getEncoder().encodeToString(Signature.sign("Sign".getBytes(StandardCharsets.UTF_8), w.getPriKey()))
        );
        t.setExtraNonce(new byte[]{0, 0});
        System.out.println(t.getHash());
        Transaction l = Transaction.fromString(t.toString());
        System.out.println(l.getHash());
    }

    /**
     * Converts the String format of a Transaction into a useable Transaction
     *
     * @param input The String-format of a Transaction
     * @return The Transactional equivalent to the String input
     */
    public static Transaction fromString(String input) {
        Transaction t;
        String[] firstSplit = input.split("->");
        String senderAddress = firstSplit[0];
        BigDecimal amount = new BigDecimal(firstSplit[1]);

        String[] secondSplit = firstSplit[2].split("\\|");

        String receiverAddress = secondSplit[0];
        String transactionID = secondSplit[1];
        String[] thirdSplit = secondSplit[2].split("\\{");

        String signature = thirdSplit[0];
        String[] fourthSplit = thirdSplit[1].split(",");
        byte extraNonce1 = Byte.parseByte(fourthSplit[0]);
        byte extraNonce2 = Byte.parseByte(fourthSplit[1].substring(0, fourthSplit[1].length() - 1));

        t = new Transaction(senderAddress, amount, receiverAddress);
        t.setTXID(transactionID);
        t.addSignature(signature);
        t.setExtraNonce(new byte[]{extraNonce1, extraNonce2});
        return t;
    }

    public static Transaction createRandomTransaction() {
        Wallet w = Wallet.createNewWallet("None");
        Wallet f = Wallet.createNewWallet("None");
        BigDecimal amount = BigDecimal.valueOf(System.currentTimeMillis() / 100000);

        Transaction t = new Transaction(w, amount, f);
        t.setTXID("1-" + System.currentTimeMillis());
        w.signTransaction(t);
        return t;
    }

    public String getReceiverAddr() {
        return receiverAddr;
    }

    public void setReceiverAddr(String receiverAddr) {
        this.receiverAddr = receiverAddr;
    }

    public BigDecimal getFullPaid() {
        return amount;
    }

    public BigDecimal getTransactionFees() {
        return amount.multiply(new BigDecimal("0.01"));
    }

    public BigDecimal getAmountWithoutFees() {
        return amount.multiply(new BigDecimal("0.99"));
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * A getter that returns the Transaction ID. Can return null if the TXID hasn't been set yet.
     *
     * @return Returns the Transaction ID assosciated with this Transaction (Millisecond time and block #)
     */
    public String getTXID() {
        return this.transactionID;
    }

    /**
     * Used to set the Transaction ID for a transaction, and to subsequently sign it.
     *
     * @param id The Transaction ID
     */
    public void setTXID(String id) {
        this.transactionID = id;
    }

    /**
     * Sets the Transaction's Signature
     *
     * @param signature The signature (ECDSA-private-key signed) of the Transaction ID.
     */
    public void addSignature(String signature) {
        this.signature = signature;
    }

    /**
     * A getter that returns the Signature. Can return null if the Signature hasn't been set yet.
     *
     * @return Returns the Signature assosciated with this Transaction (Private-key-encrypted version of the TXID)
     */
    public String getSignature() {
        return this.signature;
    }

    @Override
    public String toString() {
        String fullTX = senderAddr + "->" + amount.toString() + "->" + receiverAddr + "|" + transactionID + "|" + signature + "{"
                + extraNonce[0] + "," + extraNonce[1] + "}";
        return fullTX;

    }

    /**
     * Gets the Double-Hash of the current Transaction
     *
     * @return The Sha-256 Double-Hash of the Transaction, including the extra-nonce as a number
     */
    public String getHash() {
        assert (receiverAddr != null);
        assert (senderAddr != null);
        assert (amount != null);
        assert (transactionID != null);
        assert (signature != null);
        String fullTX = this.toString();

        return Hash.hashToHex(
                Hash.hash(
                        Hash.hash(fullTX.getBytes(StandardCharsets.UTF_8))
                )
        );

    }

    public boolean verify() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        return Signature.verifySignature(
                Base64.getDecoder().decode(this.signature),
                this.getTXID().getBytes(StandardCharsets.UTF_8),
                KeyPackager.unpackagePubkey(this.getSenderAddr())
        );
    }

    public byte[] getExtraNonce() {
        return extraNonce;
    }

    public void setExtraNonce(byte[] arr) {
        assert (arr.length == 2);
        this.extraNonce = arr;
    }

    public String getSenderAddr() {
        return senderAddr;
    }

    public void setSenderAddr(String senderAddr) {
        this.senderAddr = senderAddr;
    }

    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return senderAddr.equals(that.senderAddr) && receiverAddr.equals(that.receiverAddr) && amount.equals(that.amount) && Objects.equals(signature, that.signature) && Objects.equals(transactionID, that.transactionID) && Arrays.equals(extraNonce, that.extraNonce);
    }

}
