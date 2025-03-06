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
     * So, here we’re making three specific checks for every block:
     * (a) The stored hash of the current block is actually what it calculates
     * (b) The hash of the previous block stored in the current block is the hash of the previous block
     * (c) The current block has been mined
     *
     * @return True/False -> (a==True) && (b==True) && (c==True)
     */
    public boolean checkCurrentChain(){
        if (blockchain.size() == 0 ) return true;

        if (blockchain.size() == 1)
            return blockchain.get(0).getBlockHash().equals(blockchain.get(0).calculateBlockHash());

        boolean flag = true;
        for (int i = 0 ; i < blockchain.size() ; i++){
            String previousHash = i==0 ? "" : blockchain.get(i - 1).getBlockHash();
            String prefixString = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');

            boolean a = blockchain.get(i).getBlockHash().equals(blockchain.get(i).calculateBlockHash());
            boolean b = previousHash.equals(blockchain.get(i).getPreviousBlockHash());
            boolean c = blockchain.get(i).getBlockHash().substring(0,Constants.DIFFICULTY).equals(prefixString);
            flag = a && b && c;

            if (!flag){
                System.out.println("   a :" + a );
                System.out.println("   b :" + b );
                System.out.println("   c :" + c );
                break;
            }
        }
        return flag;
    }


    /**
     * Check if the current block in the chain are valid, if so
     * add's the given block to the blochain
     * @param b
     * @return {@code true} if the the block was added, {@code false} otherwise
     */
    public boolean addBlock(Block b){
        if(!checkCurrentChain()) return false;
        this.blockchain.add(b);
        this.lastBlock = b;
        return true;

    }


    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (Block b: this.blockchain)
                ans.append(b.toString()).append("\n");
        return ans.toString();
    }
}
