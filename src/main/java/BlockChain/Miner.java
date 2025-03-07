package BlockChain;

import Cryptography.CryptoUtils;

import java.security.*;

/**
 * This class represents some node trying to mine a block and add it to the Blockchain
 *
 */
public class Miner {

    // TODO : definir o valor do reward por ter um mining sucedido
    private double reward;
    private Block minedBlock;

    private PrivateKey privateKey;
    public  PublicKey publicKey;

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

    /* Auxiliar method's */

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
     * --> Sign's the mined block with Miner's Digital Signature
     * --> Sets the publick key of miner that mined the block (to facilitate verification)
     * TODO:(VERIFICAR) Basicaly this does Proof of Work
     * @return The newly mined Block with Miner's signature set
     */
    public Block mineBlock(String[] tranctions, String previousBlockHash) {
        this.minedBlock = new Block(tranctions,previousBlockHash);
        minedBlock = proofOfWork(minedBlock,Constants.DIFFICULTY);
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
        String blockHeader = minedBlock.getBlockHash() + minedBlock.getPreviousBlockHash() + minedBlock.getNonce() + minedBlock.getTimestamp();
        byte[] signature =  CryptoUtils.sign(privateKey,blockHeader.getBytes());
        minedBlock.setMinerSignature(signature);
    }

    /**
     * TODO: verificar se PoW está bem feito
     * Mines a block with a given dificulty (POW) and return's
     * @param b
     * @param dificulty
     * @return
     */
    public Block proofOfWork(Block b, int dificulty){
        String prefixString = new String(new char[dificulty]).replace('\0','0');
        String hash = b.getBlockHash();

        while (!hash.substring(0,dificulty).equals(prefixString)){
            b.incrementNonce();
            b.calculateBlockHash();
            hash = b.getBlockHash();
            //System.out.println(hash.substring(0,dificulty));
        }
        //TODO: Add reward to miner
        System.out.println("Mined Block !! ");
        System.out.println("Block hash: " + b.getBlockHash());
        System.out.println("Nonce: " + b.getNonce());
        return b;
    }
}
