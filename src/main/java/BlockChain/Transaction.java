package BlockChain;
import Cryptography.CryptoUtils;

import java.io.Serializable;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

/**
 * Class that represents a Transaction our action
 * aplication.
 */
public class Transaction implements Serializable {
    public enum TransactionType {
         START_AUCTION, CLOSE_AUCTION, PLACE_BID
    }
    private final String owner; // owner of auction
    private final String username; // user that made this Trasanction
    private final TransactionType type;
    private final String transactionId; // Uniquely identifies a Single transaction
    private final String auctionId; // Uniquely identifies an auction owner:auctionName
    private final long timestamp;
    private final double bidAmount; // Only used in "PLACE_BID"

    /**
     * Creates a Transaction in our auction aplication
     * @param owner        Owner of the auction
     * @param username     Username that created this transaction
     * @param type         Type of transaction (check TransactionType to see available types)
     * @param auctionName  Name of the auction
     * @param bidAmount    Amount of money
     * @param timestamp
     */
    public Transaction(String owner,String username, TransactionType type, String auctionName, double bidAmount, long timestamp) {
        this.owner = owner;
        this.username = username;
        this.type = type;
        this.auctionId = owner + ":" + auctionName;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
        this.transactionId = generateTransactionId();
    }

    /* Getter's */
    public String getTransactionId() {
        return transactionId;
    }
    public String getOwner() { return owner; }
    public TransactionType getType() { return type; }
    public String getAuctionId() {
        return auctionId;
    }

    public String getUsername() { return username; }

    public double getBidAmount() {
        return bidAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /* Auxiliar methods  */

    /**
     * generates a transaction Id based on Transaction data
     * @return genarated Transaction Id
     */
    private String generateTransactionId() {
        return CryptoUtils.getHash256((owner + type + auctionId + bidAmount + timestamp));
    }

    @Override
    public String toString() {
        return "\n\tTransaction Details:\n" +
                "\t\tOwner: " + owner + "\n" +
                "\t\tUser that made transaction: " + username + "\n" +
                //"\t\tTransaction Id: " + transactionId + "\n" +
                "\t\tAuction Id: " + auctionId + "\n" +
                "\t\tTransaction type: " + type + "\n" +
                "\t\tBid ammount:  " + bidAmount + "\n" +
                "\t\tTimestamp: " + Utils.convertTime(timestamp) + "\n";
    }

}
