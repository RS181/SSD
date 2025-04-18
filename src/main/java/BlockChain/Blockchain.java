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

    /* Getter's & Setter's */
    public List<Block> getBlockchain() {
        return blockchain;
    }

    public Block getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(Block lastBlock) {
        this.lastBlock = lastBlock;
    }

    /* Auxiliar methods */

    /**
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
     * if the current  chain is valid.If so adds the given block to the blochain
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
     * Returns a set of available auction IDs based on the current state of the blockchain.
     * <p>
     * This method iterates through all blocks in the blockchain and examines their transactions
     * to determine which auctions are currently available. An auction is considered available
     * if it has been {@code START_AUCTION} and has not been {@code CLOSE_AUCTION} afterward —
     * even if {@code CLOSE_AUCTION} appears before {@code START_AUCTION} in the chain.
     * </p>
     *
     * @return A set containing the IDs of auctions that are currently available
     *         (i.e., started and not closed).
     *
     * @implNote The responsibility for ensuring that transactions follow a valid logical sequence
     *           is handled by the {@code ClientHandler} class. When a client submits a transaction,
     *           {@code ClientHandler} checks whether it is valid within the current state of the system.
     */
    public Set<String> getAvailableAuctions() {
        Set<String> ans = new HashSet<>();
        Set<String> closed = new HashSet<>();

        for (Block b : blockchain) {
            ArrayList<Transaction> transactionsInBlock = b.getTransactions();
            for (Transaction t : transactionsInBlock) {
                String auctionId = t.getAuctionId();
                Transaction.TransactionType auctionType = t.getType();

                if (auctionType == Transaction.TransactionType.CLOSE_AUCTION) {
                    closed.add(auctionId); // Mark as closed, regardless of previous START
                    ans.remove(auctionId); // Ensure it's no longer available
                } else if (auctionType == Transaction.TransactionType.START_AUCTION) {
                    if (!closed.contains(auctionId)) {
                        ans.add(auctionId); // Only add if not previously closed
                    }
                }
            }
        }
        return ans;
    }

    /**
     * Retrieves all bid entries made for a specific auction, formatted as strings.
     * <p>
     * Each entry in the returned set is a string in the format {@code "username:bidAmount"},
     * representing a bid placed by a user on the given auction.
     * <p>
     * The method performs the following steps:
     * <ol>
     *   <li>Checks if the specified auction ID exists in the set of currently available auctions.</li>
     *   <li>If the auction is not available, the method returns {@code null}.</li>
     *   <li>If available, iterates over the blockchain to find all {@code PLACE_BID} transactions
     *       related to the specified auction ID.</li>
     *   <li>For each matching bid, adds a string entry in the format {@code "username:bidAmount"} to the result set.</li>
     * </ol>
     *
     * @param auctionId the ID of the auction for which bids are to be retrieved
     *                  of auctionId = "owner:auctionName"
     * @return a {@link Set} of bid strings in the format {@code "username:bidAmount"}, or {@code null} if the auction ID is not valid
     */
    public Set<String> getAllBids(String auctionId){
        Set<String> ans = new HashSet<>();
        Set<String> aux = getAvailableAuctions();

        // 1. check if exists an available auction with this auctionId
        if (!aux.contains(auctionId)) return null;

        // 2. Get all the PLACE_BID made for auction with auctionId
        for (Block b : blockchain){
            ArrayList<Transaction> transactionsInBlock = b.getTransactions();
            for (Transaction t : transactionsInBlock) {
                String aucId = t.getAuctionId();
                if(aucId.equals(auctionId) && t.getType().equals(Transaction.TransactionType.PLACE_BID))
                    ans.add(t.getUsername() + ":" + t.getBidAmount() );
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
