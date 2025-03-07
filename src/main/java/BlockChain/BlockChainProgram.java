package BlockChain;

public class BlockChainProgram {

    public static void main(String[] args) {

        Blockchain blockchain = new Blockchain();

        String[] initialValues = {"Shad has $700", "Miguel has $550"};

        //First mined Block
        Miner m = new Miner();
        Block firstBlock = m.mineBlock(initialValues,"");
        System.out.println("First mined Block is " + firstBlock.toString());
        //System.out.println("The block chain is " + blockChain.toString());

        String[] shadGivesItAway = {"Shad gives Tim $40" , "Shad gives Tany $60" ,"Shad gives Terry $100"};

        //Second mined Block
        m = new Miner();
        Block secondBlock = m.mineBlock(shadGivesItAway,firstBlock.getBlockHash());
        System.out.println("Second mined Block is " + secondBlock.toString());
        //System.out.println("The block chain is " + blockChain.toString());

        String[] shadGetsSome = {"Tim gives Shad $10" , "Terry gives $50 to Shad" };

        //Third mined Block
        m = new Miner();
        Block thirdBlock = m.mineBlock(shadGetsSome,secondBlock.getBlockHash());
        System.out.println("Third mined Block is " + thirdBlock.toString());
        //System.out.println("The block chain is " + blockChain.toString());

        System.out.println("Adicionar primeiro bloco : " + blockchain.addBlock(firstBlock,m));
        System.out.println("Adicionar segundo bloco : " + blockchain.addBlock(secondBlock,m));
        System.out.println("Adicionar terceiro bloco : " + blockchain.addBlock(thirdBlock,m));

        System.out.println(blockchain);
        System.out.println(blockchain.checkCurrentChain());



    }



}
