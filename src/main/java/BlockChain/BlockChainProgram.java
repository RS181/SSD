package BlockChain;

import java.util.ArrayList;
import java.util.List;

public class BlockChainProgram {

    public static void main(String[] args) {

        Blockchain blockchain = new Blockchain();
        Miner user1 = new Miner();
        Miner user2 = new Miner();

        ArrayList<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(user1.getPublicKey(), Transaction.TransactionType.CREATE_AUCTION, "AUC123", 0, System.currentTimeMillis()));
        transactions.add(new Transaction(user2.getPublicKey(), Transaction.TransactionType.PLACE_BID, "AUC123", 100.50, System.currentTimeMillis()));
        transactions.add(new Transaction(user1.getPublicKey(), Transaction.TransactionType.PLACE_BID, "AUC123", 150.75, System.currentTimeMillis()));
        transactions.add(new Transaction(user2.getPublicKey(), Transaction.TransactionType.CLOSE_AUCTION, "AUC123", 0, System.currentTimeMillis()));

        Block firstBlock = user1.mineBlock(transactions,"");
    }



}
