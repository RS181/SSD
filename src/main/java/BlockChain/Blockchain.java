package BlockChain;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of blocks and a method to verify the validity of the chain that is stored
 */
public class Blockchain {

    private List<Block> blockchain = new ArrayList<>();
    private Block lastBlock;


    /**
     * Default constructor
     */
    public Blockchain(){
    }


    /**
     * Constructor that receives a ArrayList of blocks and creates the Blockchain object
     * @param blockChainList
     */
    public Blockchain(ArrayList<Block> blockChainList){
        this.blockchain = blockChainList;
    }


    /* Getter's & Setter's */

    public List<Block> getBlockchain() {
        return blockchain;
    }

    public Block getLastBlock() {
        return lastBlock;
    }

    /**
     * TODO: Verificar se a lógica esta correta
     * Check's all block's in the  blockchain (starting from the first)
     * Here we just check the hash
     * True/False -> Hash(block_k) == Previous_Hash(block_k-1) (k >= 1)
     * @return
     */
    public boolean checkCurrentChain(){
        if (blockchain.size() <= 1 ) return true;

        for (int i = 1 ; i < blockchain.size() ; i++){
            Block previous = blockchain.get(i-1);
            Block current = blockchain.get(i);
            previous.calculateBlockHash();
            if (!current.getPreviousBlockHash().equals(previous.getBlockHash()))
                return false;
            //System.out.println(i + " " + (i-1)+ " = " + !current.getBlockHash().equals(current.getPreviousBlockHash()));
        }
        return true;
    }


    /**
     * Add's the given block to the blochain
     * @param b
     */
    public void addBlock(Block b){
        this.blockchain.add(b);
        this.lastBlock = b;
    }


    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (Block b: this.blockchain)
                ans.append(b.toString()).append("\n");
        return ans.toString();
    }
}
