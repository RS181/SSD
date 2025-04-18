package BlockChain;

import Cryptography.CryptoUtils;

import java.io.Serializable;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This class represents a Miner, that will mine blocks with
 * Proof-Of-Work consensus algorithm
 */
public class Miner implements Serializable {
    private Block minedBlock;
    private PrivateKey privateKey;
    public  PublicKey publicKey;
    private boolean stopMining; // indicates if a miner has to stop mining

    /**
     * Default Constructor of a Miner:
     * --> Create Key pair for this miner
     */
    public Miner (){
        createKeyPair();
    }

    public Miner (PrivateKey privateKey, PublicKey publicKey){
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /* Getter's */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    // JUST FOR TESTING PURPOSES (SHOULD NOT BE ABLE TO GET PRIV KEY OF MINER)
    public PrivateKey getPrivateKey(){ return privateKey; }

    /* Auxiliar methods */

    /**
     * Creates a Public and Private Key for miner, so
     * that he can sign the block's that he mined
     */
    private void createKeyPair(){
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: While trying to create Key pair for Miner ");
            throw new RuntimeException(e);
        }
    }

    /**
     * Mines a new block from the given list of transactions and the hash of the previous block.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Sorts the list of transactions in ascending order based on their timestamp
     *         (to ensure a consistent transaction order for block hashing across all nodes).</li>
     *     <li>Creates a new block with the sorted transactions and the previous block's hash.</li>
     *     <li>Executes the Proof of Work algorithm to mine the block.</li>
     *     <li>Signs the mined block with the miner's digital signature.</li>
     *     <li>Attaches the miner's public key to the block for signature verification purposes.</li>
     * </ul>
     * If the mining process is interrupted (i.e., a stop signal is received), the method returns {@code null}.
     *
     * @param tranctions        List of transactions to include in the block.
     * @param previousBlockHash Hash of the most recent block in the blockchain.
     * @return The newly mined and signed {@code Block}, or {@code null} if mining was interrupted.
     */
    public Block mineBlock(ArrayList<Transaction> tranctions, String previousBlockHash) {
        // Sort transactions by timestamp in ascending order
        tranctions.sort(Comparator.comparingLong(Transaction::getTimestamp));
        this.minedBlock = new Block(tranctions,previousBlockHash);
        System.out.println("Before Mining " + stopMining);
        minedBlock = proofOfWork(minedBlock,Constants.DIFFICULTY);
        if (stopMining) { // Check if miner 'received' stop signal (if so return null)
            stopMining = false; // reset
            return null;
        }
        //Signs the block
        signBlockHeader();
        // Sets the public key of miner (to allow for later verification of Signature)
        minedBlock.setMinerPublicKey(publicKey);
        return minedBlock;
    }

    /**
     * Sign's the  minedBlock header
     */
    public void signBlockHeader(){
        String blockHeader =
                minedBlock.getBlockHash() + minedBlock.getPreviousBlockHash() + minedBlock.getNonce()
                + minedBlock.getTimestamp() + minedBlock.getTransactions();
        byte[] signature =  CryptoUtils.sign(privateKey,blockHeader.getBytes());
        minedBlock.setMinerSignature(signature);
    }

    /**
     * Mines a block with a given dificulty (POW) and return's
     * @param b
     * @param dificulty
     * @return
     */
    public Block proofOfWork(Block b, int dificulty){
        String prefixString = new String(new char[dificulty]).replace('\0','0');
        String hash = b.getBlockHash();
        while (!hash.substring(0,dificulty).equals(prefixString) && !stopMining){
            b.incrementNonce();
            b.calculateBlockHash();
            hash = b.getBlockHash();
            //System.out.println(hash.substring(0,dificulty));
        }

        if(!stopMining) {
            System.out.println("Mined Block !! ");
            System.out.println("Block hash: " + b.getBlockHash());
            System.out.println("Nonce: " + b.getNonce());
        }else {
            System.out.println("Mining was STOPPED !!");
        }
        return b;
    }

    public void stopMining(){
        stopMining = true;
    }
    public void canStartMining(){stopMining = false;}
}
