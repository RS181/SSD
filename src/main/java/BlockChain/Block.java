package BlockChain;

import Cryptography.CryptoUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

public class Block {

    //TODO: Criar uma classe para transações
    private  String[] transactions;
    private String blockHash;
    private String  previousBlockHash;


    // TODO: no timestamp o valor é o UTC?
    private long timestamp;
    //TODO: qual o valor incial da nonce? ( pode ser 0 e depois vamos incrementando )
    private int nonce;


    public Block(String[] transactions, String previousBlockHash) {
        this.transactions = transactions;
        this.previousBlockHash = previousBlockHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        calculateBlockHash();
    }


    /* Getter's & Setter's */
    public String getBlockHash() {
        return blockHash;
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


    /* Auxiliar method's */

    /**
     * Calculates the hash of the current Block
     *
     * TODO Confirmar esta definição para o calculo do BlockHash
     *
     * BlockHash = previousBlockHash +  timestamp + nonce
     */
    public String calculateBlockHash(){
        String input = previousBlockHash + timestamp + nonce; //+ Arrays.toString(transactions);
        this.blockHash = CryptoUtils.getHash(input);
        return this.blockHash;
    }
    @Override
    public String toString() {
        return "{ PreviousBlockHash = " + this.previousBlockHash + ", " +
                "Timestamp = " + Utils.convertTime(this.timestamp) +  ", " +
                "BlockHash = " + this.blockHash + ", " +
                "Nonce = " + this.nonce  + ", "+
                "Transactions = " + Arrays.toString(transactions) + " }" ;
    }
}
