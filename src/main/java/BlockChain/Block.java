package BlockChain;

import Cryptography.CryptoUtils;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Class that represent's a block that will be used
 * to construct the blockchain
 */
public class Block implements Serializable {
    private ArrayList<Transaction> transactions;
    private String blockHash;
    private String  previousBlockHash;
    private long timestamp;
    private int nonce;
    private  byte[] minerSignature = null; // Digital Signature of this block's header with Priv. Key of the respectiv miner
    private PublicKey minerPublicKey = null; // Public key of this block miner

    /**
     * Constructor for a Block
     * @param transactions       List of transaction that will be a part of this block
     * @param previousBlockHash  The hash of the last block in the blockchain
     */
    public Block(ArrayList<Transaction> transactions, String previousBlockHash) {
        this.transactions = transactions;
        this.previousBlockHash = previousBlockHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        calculateBlockHash();
    }

    /* Getter's & Setter's (most Setter's are there just for testing purposes) */

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public byte[] getMinerSignature() {
        return minerSignature;
    }

    public PublicKey getMinerPublicKey() {
        return minerPublicKey;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void incrementNonce(){
        this.nonce++;
    }

    public void setMinerSignature(byte[] minerSignature) {
        if (this.minerSignature == null)
            this.minerSignature =  minerSignature;
        else
            System.out.println("Block already has a signature");
    }

    public void forceSetMinerSignature(byte[] minerSignature){
        this.minerSignature = minerSignature;
    }

    public void setMinerPublicKey(PublicKey minerPublicKey) {
        this.minerPublicKey = minerPublicKey;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }


    /* Auxiliar methods */

    /**
     * Calculates the hash of the current Block
     * BlockHash = previousBlockHash +  timestamp + nonce + transactions
     */
    public String calculateBlockHash(){
        String input = previousBlockHash + timestamp + nonce + transactions;
        this.blockHash = CryptoUtils.getHash256(input);
        return this.blockHash;
    }
    @Override
    public String toString() {
        return "{ PreviousBlockHash = " + this.previousBlockHash + ", " +
                "Timestamp = " + Utils.convertTime(this.timestamp) +  ", " +
                "BlockHash = " + this.blockHash + ", " +
                "Nonce = " + this.nonce  + ", "+
                "Transactions = " +transactions + " }" ;
    }
}
