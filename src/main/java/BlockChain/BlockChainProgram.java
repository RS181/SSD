package BlockChain;

import java.util.ArrayList;

public class BlockChainProgram {

    public static void main(String[] args) {

        Blockchain blockchain = new Blockchain();
        Miner user1 = new Miner();
        Miner user2 = new Miner();

        ArrayList<Transaction> firstTransactions = new ArrayList<>();
        firstTransactions.add(new Transaction("user1","user1", Transaction.TransactionType.START_AUCTION, "AUC123", 0, System.currentTimeMillis()));
        firstTransactions.add(new Transaction("user2","user2", Transaction.TransactionType.PLACE_BID, "AUC123", 100.50, System.currentTimeMillis()));
        firstTransactions.add(new Transaction("user1","user2" ,Transaction.TransactionType.PLACE_BID, "AUC123", 150.75, System.currentTimeMillis()));
        Block firstBlock = user1.mineBlock(firstTransactions,"");
        System.out.println("Add result = " + blockchain.addBlock(firstBlock,user1.getPublicKey()));
        System.out.println("Blockchain status = " + blockchain.checkCurrentChain());
        System.out.println(blockchain.getAvailableAuctions());


        ArrayList<Transaction> secondTransactions = new ArrayList<>();
        secondTransactions.add(new Transaction("user1","user1", Transaction.TransactionType.START_AUCTION, "batatas", 0, System.currentTimeMillis()));
        secondTransactions.add(new Transaction("user1","user1" ,Transaction.TransactionType.CLOSE_AUCTION, "AUC123", 0, System.currentTimeMillis()));

        Block secondBlock = user2.mineBlock(secondTransactions,blockchain.getLastBlock().getBlockHash());
        System.out.println("Add result = " + blockchain.addBlock(secondBlock,user2.getPublicKey()));
        System.out.println("Blockchain status = " + blockchain.checkCurrentChain());
        System.out.println(blockchain.getAvailableAuctions());

        ArrayList<Transaction> thirdTransactions = new ArrayList<>();
        thirdTransactions.add(new Transaction("user1","user1", Transaction.TransactionType.START_AUCTION, "AUC123", 0, System.currentTimeMillis()));
        thirdTransactions.add(new Transaction("user1","user1", Transaction.TransactionType.PLACE_BID, "batatas", 100, System.currentTimeMillis()));
        thirdTransactions.add(new Transaction("user1","user2", Transaction.TransactionType.PLACE_BID, "batatas", 200, System.currentTimeMillis()));

        Block thirdBlock = user2.mineBlock(thirdTransactions,blockchain.getLastBlock().getBlockHash());
        System.out.println("Add result =" +  blockchain.addBlock(thirdBlock, user2.getPublicKey()));
        System.out.println("Blockchain status = " + blockchain.checkCurrentChain());
        System.out.println(blockchain.getAvailableAuctions());

        System.out.println(blockchain);
        System.out.println(blockchain.getAllBids("user1:batatas"));

    }



}
