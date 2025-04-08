package BlockChain;

import Cryptography.CryptoUtils;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.*;

/**
 * This class contains a list of blocks and a methods to:
 * -> verify the validity of the chain that is stored
 * -> add valid blocks to the chain
 *
 */
public class Blockchain implements Serializable {

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
     * (c) The current block has been mined (PoW)
     * (d) The block has a valid miner Signature
     * @return True/False -> (a==True) && (b==True) && (c==True) && (d==True)
     */
    public boolean checkCurrentChain(){
        String prefixString = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        boolean a,b,c,d;
        a = b = c = d = true;
        boolean flag = true;
        for (int i = 0 ; i < blockchain.size() ; i++){
            String previousHash = i==0 ? "" : blockchain.get(i - 1).getBlockHash();
            Block currentBlock = blockchain.get(i);
            byte[] minerSignature = currentBlock.getMinerSignature();
            PublicKey minerPublicKey = currentBlock.getMinerPublicKey();
            String blockHeader = currentBlock.getBlockHash() + currentBlock.getPreviousBlockHash()
                    + currentBlock.getNonce() + currentBlock.getTimestamp() + currentBlock.getTransactions();


            a = currentBlock.getBlockHash().equals(currentBlock.calculateBlockHash());
            b = previousHash.equals(currentBlock.getPreviousBlockHash());
            c = currentBlock.getBlockHash().substring(0,Constants.DIFFICULTY).equals(prefixString);
            d = CryptoUtils.verifySignature(minerPublicKey,blockHeader.getBytes(),minerSignature);
            flag = a && b && c && d;

            if (!flag){
                System.out.println("Block [" + i  + "] in the Blockchain failed the check:");
                System.out.println("   a :" + a );
                System.out.println("   b :" + b );
                System.out.println("   c :" + c );
                System.out.println("   d :" + d );
                break;
            }
        }
        return flag;
    }

    /**
     * Check if the block we are trying to add to the chain is valid and also check
     * if the current  chain is valid.If so add's the given block to the blochain
     * @param block             that we want to add to the blockchain
     * @param minerPublickKey   publick key of the miner that suposedely mined the block
     *
     * @return {@code true} if  the block was added, {@code false} otherwise
     */
    public boolean addBlock(Block block,PublicKey minerPublickKey){
        if (!validateBlock(block,minerPublickKey)) return false;

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
     * (c) The block has been mined (PoW)
     * (d) The Digital Signature of Miner of the block is valid
     *
     * @param block that we are validating
     * @param minerPublickKey publick key of the miner that suposedely mined the block
     */
    private boolean validateBlock(Block block,PublicKey minerPublickKey){
        String prefixString = new String(new char[Constants.DIFFICULTY]).replace('\0', '0');
        String blockHeader =
                block.getBlockHash() + block.getPreviousBlockHash() + block.getNonce() + block.getTimestamp() + block.getTransactions();
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
            d = CryptoUtils.verifySignature(minerPublickKey,blockHeader.getBytes(),minerSignature);

        return a && b && c && d;
    }

    /**
     * Returns a set of available auction IDs based on the auction life cycle.
     * <p>
     * This method traverses all blocks in the blockchain and processes their transactions to
     * determine which auctions are currently available. An auction is considered available if it has
     * been created and started but not yet closed. The expected logical flow of an auction is:
     * <pre>
     * CREATE_AUCTION -> START_AUCTION -> PLACE_BID(s) -> CLOSE_AUCTION
     * </pre>
     *
     * @return A set containing the IDs of auctions that are currently available
     *         (i.e., started but not closed).
     *
     * @implNote The logic that ensures transactions follow a valid sequence and structure
     *           is handled in the {@code ClientHandler} class. When a client submits a transaction,
     *           {@code ClientHandler} verifies whether the transaction is valid and logically consistent
     *           with the current state of the auction system.
     */
    public Set<String> getAvailableAuctions(){
        Set<String> ans = new HashSet<>();
        Set<String> aux  = new HashSet<>();
        for(Block b : blockchain){
            ArrayList<Transaction> transactionsInBlock = b.getTransactions();
            for(Transaction t: transactionsInBlock){
                String auctionId = t.getAuctionId();
                Transaction.TransactionType auctionType = t.getType();
                //TODO
            }
        }
        return ans;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (Block b: this.blockchain)
                ans.append(b.toString()).append("\n");
        return ans.toString();
    }
}
