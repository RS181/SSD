package BlockChain;

import Cryptography.CryptoUtils;

import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This class represents some node trying to mine a block and add it to the Blockchain
 *
 */
public class Miner {
    private Block minedBlock;
    private PrivateKey privateKey;
    public  PublicKey publicKey;
    private boolean stopMining; // indicates if a miner has to stop mining

    /**
     * Constructor of a Miner:
     * --> Create Key pair for this miner
     */
    public Miner (){
        createKeyPair();
    }


    /* Getter's */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    // JUST FOR TESTING PURPOSES (SHOULD NOT BE ABLE TO GET PRIV KEY OF MINER)
    public PrivateKey getPrivateKey(){ return privateKey; }

    /* Auxiliar method's */

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
     * This function returns the newly created block ONLY after the mineBlock() method is executed.
     * Also it :
     * --> Sort's the list of transanctions by transaction Id
     * --> Sign's the mined block with Miner's Digital Signature
     * --> Sets the publick key of miner that mined the block (to facilitate verification)
     * @param tranctions list of transanctions
     * @param previousBlockHash Hash of last block in the blockchain
     * @return The newly mined Block with Miner's signature set
     */
    public Block mineBlock(ArrayList<Transaction> tranctions, String previousBlockHash) {
        // Order the transction by TransactionId (so that everyone sees the transaction
        // in the same order and therefore everyone will calculate the same block hash)
        tranctions.sort(Comparator.comparing(Transaction::getTransactionId));
        this.minedBlock = new Block(tranctions,previousBlockHash);
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
}
