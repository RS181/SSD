package BlockChain;
import Cryptography.CryptoUtils;

import java.io.Serializable;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

/*Class that represents a Transaction (Operation) */
public class Transaction implements Serializable {
    public enum TransactionType {
        CREATE_AUCTION, START_AUCTION, CLOSE_AUCTION, PLACE_BID
    }
    private final String owner;
    private final TransactionType type;
    private final String transactionId;
    private final String auctionId;
    private final long timestamp;
    private final double bidAmount; // Only used in "PLACE_BID"


    public Transaction(String owner, TransactionType type, String auctionId, double bidAmount, long timestamp) {
        this.owner = owner;
        this.type = type;
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
        this.transactionId = generateTransactionId();
    }

    /**
     * generates a tranction Id based on Transaction data
     * @return genarated Transaction Id
     */
    private String generateTransactionId() {
        return CryptoUtils.getHash256((owner + type + auctionId + bidAmount + timestamp));
    }


    @Override
    public String toString() {
        return "\n\tTransaction Details:\n" +
                "\t\tOwner: " + owner + "\n" +
                "\t\tTransaction Id: " + transactionId + "\n" +
                "\t\tAuction Id: " + auctionId + "\n" +
                "\t\tTransaction type: " + type + "\n" +
                "\t\tBid ammount:  " + bidAmount + "\n" +
                "\t\tTimestamp: " + Utils.convertTime(timestamp) + "\n";
    }

    /*Getter's*/
    public String getTransactionId() {
        return transactionId;
    }
    public String getOwner() { return owner; }
    public TransactionType getType() {
        return type;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
