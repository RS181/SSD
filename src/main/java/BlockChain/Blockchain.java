package BlockChain;

import Cryptography.CryptoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of blocks and a methods to:
 * -> verify the validity of the chain that is stored
 * -> add valid blocks to the chain
 *
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

    /* Getter's  */
    public List<Block> getBlockchain() {
        return blockchain;
    }

    public Block getLastBlock() {
        return lastBlock;
    }

    /* Auxiliar methods */

    /**
     * TODO: Verificar se a lógica esta correta
     * Check's all block's in the  blockchain (starting from the first)
     * So, here we’re making three specific checks for every block:
     * (a) The stored hash of the current block is actually what it calculates
     * (b) The hash of the previous block stored in the current block is the hash of the previous block
     * (c) The current block has been mined
     * @return True/False -> (a==True) && (b==True) && (c==True)
     */
    public boolean checkCurrentChain(){
        String prefixString = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        boolean a,b,c;
        a = b = c = true;
        boolean flag = true;
        for (int i = 0 ; i < blockchain.size() ; i++){
            String previousHash = i==0 ? "" : blockchain.get(i - 1).getBlockHash();

            a = blockchain.get(i).getBlockHash().equals(blockchain.get(i).calculateBlockHash());
            b = previousHash.equals(blockchain.get(i).getPreviousBlockHash());
            c = blockchain.get(i).getBlockHash().substring(0,Constants.DIFFICULTY).equals(prefixString);
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
     * Check if the block we are trying to add to the chain is valid and also check
     * if the current  chain is valid.If so add's the given block to the blochain
     * @param block that we want to add to the blockchain
     * @param miner that mined the block
     *
     * @return {@code true} if  the block was added, {@code false} otherwise
     */
    public boolean addBlock(Block block,Miner miner){
        String prefixString = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        if (!validateBlock(block,miner)) return false;

        if(!checkCurrentChain()) return false;
        this.blockchain.add(block);
        this.lastBlock = block;
        return true;
    }

    /**
     * Checks if the block received has an argument is valid by:
     * (a) The stored hash of the current block is actually what it calculates
     * (b) The hash of the last block in the chain is equal to the previous hash of
     * the block we are trying to add
     * (c) The block has been mined
     * (d) The Digital Signature of Miner of the block is valid
     *
     * @param block that we are validating
     * @param miner that suposedely mined th block
     */
    private boolean validateBlock(Block block,Miner miner){
        String prefixString = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        String blockHeader =
                block.getBlockHash() + block.getPreviousBlockHash() + block.getNonce() + block.getTimestamp();
        byte[] minerSignature = block.getMinerSignature();
        boolean a,b,c,d;
        a = b = c = d = true;

        a = block.getBlockHash().equals(block.calculateBlockHash());
        if (lastBlock != null)
            b = block.getPreviousBlockHash().equals(lastBlock.getBlockHash());
        c = block.getBlockHash().substring(0, Constants.DIFFICULTY).equals(prefixString);
        if (minerSignature == null)
            d = false;
        else
            d = CryptoUtils.verifySignature(miner.publicKey,blockHeader.getBytes(),minerSignature);

        return a && b && c && d;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (Block b: this.blockchain)
                ans.append(b.toString()).append("\n");
        return ans.toString();
    }
}
