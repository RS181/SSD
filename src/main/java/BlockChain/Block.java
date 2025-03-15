package BlockChain;

import Cryptography.CryptoUtils;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Block {
    // TODO devemos 'ORDENAR' as transações (para garantir que o calculo da hash do bloco é o mesmo em todos os nós)
    // TODO ajustar representação de transações para Class Transanctions
    //private  String[] transactions;
    private ArrayList<Transaction> transactions;
    private String blockHash;
    private String  previousBlockHash;
    // TODO: no timestamp o valor é o UTC?
    private long timestamp;
    //TODO: qual o valor incial da nonce? ( pode ser 0 e depois vamos incrementando )
    private int nonce;
    private  byte[] minerSignature = null; // Digital Signature of this block's header with Priv. Key of the respectiv miner
    private PublicKey minerPublicKey = null; // Public key of this block miner

    public Block(ArrayList<Transaction> transactions, String previousBlockHash) {
        this.transactions = transactions;
        this.previousBlockHash = previousBlockHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        calculateBlockHash();
    }


    /* Getter's & Setter's (most Setter's are there just for testing purposes) */
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

    //TODO: ver qual a melhor maneira de adicionar a assinatura do Miner (senão tiver a certeza deixar assim para já)
    // TODO : PERGUNTAR AO PROFESSOR
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
    /* Auxiliar method's */

    /**
     * Calculates the hash of the current Block
     *
     * TODO Confirmar esta definição para o calculo do BlockHash
     *
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
