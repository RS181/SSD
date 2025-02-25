package BlockChain;

import java.util.Arrays;

public class Block {

    //TODO: Criar uma classe para transações
    private  String[] transactions;
    private int blockHash;
    private int previousBlockHash;

    private int nonce;
    private long timestamp;

    public Block(String[] transactions, int previousBlockHash) {
        this.transactions = transactions;
        this.previousBlockHash = previousBlockHash;

        //TODO: Ajustar para algoritmo de hash suposto (suponho que seja SHA256)
        this.blockHash = Arrays.hashCode(new int[] {Arrays.hashCode(transactions),this.previousBlockHash} );
    }


    /* Getter's & Setter's*/
    public int getBlockHash() {
        return blockHash;
    }

    public int getPreviousHash() {
        return previousBlockHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void incrementNonce(){
        this.nonce++;
    }

    @Override
    public String toString() {
        return  "PreviousBlockHash = " + this.previousBlockHash + ", "+
                //"Timestamp = " + this.timestamp +  ", "+
                "BlockHash = " + this.blockHash + ", "+
                //"Nonce = " + this.nonce  + ", "+
                "Transactions = " + Arrays.toString(transactions) + "\n";
    }
}
