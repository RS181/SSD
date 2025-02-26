package BlockChain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

public class Block {

    // Block index , just for faciliting viewing
    int index;

    //TODO: Criar uma classe para transações
    private  String[] transactions;
    private String blockHash;
    private String  previousBlockHash;

    // TODO: no timestamp o valor é o UTC?
    private long timestamp;
    //TODO: qual o valor incial da nonce? ( pode ser 0 e depois vamos incrementando )
    private int nonce;


    public Block(String[] transactions, String previousBlockHash, int index) {
        this.transactions = transactions;
        this.previousBlockHash = previousBlockHash;
        this.index = index;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        calculateBlockHash();
    }

    /* Auxiliar method's */

    /**
     * Calculates the hash of the current Block
     *
     * TODO Confirmar esta definição para o calculo do BlockHash
     *
     * BlockHash = previousBlockHash +  timestamp + nonce + transactions
     */
    public void calculateBlockHash(){
        String input = previousBlockHash + timestamp + nonce + Arrays.toString(transactions);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            this.blockHash = Utils.getHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public String toString() {
        return  "{ Index = " + this.index + ", " +
                "PreviousBlockHash = " + this.previousBlockHash + ", " +
                "Timestamp = " + this.timestamp +  ", " +
                "BlockHash = " + this.blockHash + ", " +
                "Nonce = " + this.nonce  + ", "+
                "Transactions = " + Arrays.toString(transactions) + " }" ;
    }
}
