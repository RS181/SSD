package BlockChain;

import java.util.ArrayList;

public class BlockChainProgram {

    public static void main(String[] args) {

        // Demonstrate a series of blocks in a chain
        ArrayList<Block> blockChain = new ArrayList<>();

        //First Block
        String[] initialValues = {"Shad has $700", "Miguel has $550"};
        Block firstBlock = new Block(initialValues,"",1);
        blockChain.add(firstBlock);

        System.out.println("first Block is " + firstBlock.toString());
        System.out.println("The block chain is " + blockChain.toString());

        //Second Block
        String[] shadGivesItAway = {"Shad gives Tim $40" , "Shad gives Tany $60" ,"Shad gives Terry $100"};
        Block secondBlock = new Block(shadGivesItAway,firstBlock.getBlockHash(),2);
        blockChain.add(secondBlock);

        System.out.println("Second Block is " + secondBlock.toString());
        System.out.println("The block chain is " + blockChain.toString());

        //Third Block
        String[] shadGetsSome = {"Tim gives Shad $10" , "Terry gives $50 to Shad" };
        Block thirdBlock = new Block(shadGetsSome,secondBlock.getBlockHash(),3);
        blockChain.add(thirdBlock);

        System.out.println("Third Block is " + thirdBlock.toString());
        System.out.println("The block chain is " + blockChain.toString());

    }
}
