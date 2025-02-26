package BlockChain;

/**
 * This class represents some node trying to mine a block and add it to the Blockchain
 *
 */
public class Miner {

    // TODO : definir o valor do reward por ter um mining sucedido
    private double reward;
    private Block minedBlock;


    /**
     * Default constructor
     */
    public  Miner(){}


    /**
     * Constructor of a Miner
     * Creates a Block object that has yet to be mined
     * @param transactions list of transactions
     * @param previousBlockHash hash of the prevous block in the chain
     */
    public Miner (String[] transactions, String previousBlockHash ){
        this.minedBlock = new Block(transactions,previousBlockHash);
    }


    /**
     * This function returns the newly created block ONLY after the mineBlock() method is executed.
     * TODO:(VERIFICAR) Basicaly this does Proof of Work
     * @return The newly mined Block to be added to the BlockChain
     */

    public Block getNewestBlock() {
        minedBlock = mineBlock(minedBlock,4);
        return minedBlock;
    }

    /**
     * TODO: verificar se PoW está bem feito
     * Mines a block with a given dificulty (POW) and return's
     *
     * @param b
     * @param dificulty
     * @return
     */
    public Block  mineBlock(Block b, int dificulty){
        String target = new String(new char[dificulty]).replace('\0','0');
        String hash = b.getBlockHash();

        while (!hash.substring(0,dificulty).equals(target)){
            b.incrementNonce();
            b.calculateBlockHash();
            hash = b.getBlockHash();
            System.out.println(hash.substring(0,dificulty));
        }

        //TODO: Add reward to miner

        System.out.println("Mined Block !! ");
        System.out.println("Block hash: " + b.getBlockHash());
        System.out.println("Nonce: " + b.getNonce());
        return b;

    }
}
