package BlockChain;
import Cryptography.CryptoUtils;

import java.security.*;
import java.util.Arrays;
import java.util.Base64;

/*Class that represents a Transaction (Operation) */
public class Transaction {
    public enum TransactionType {
        CREATE_AUCTION, START_AUCTION, CLOSE_AUCTION, PLACE_BID
    }
    private final PublicKey senderPublicKey;
    private final TransactionType type;
    private final String transactionId;
    private final String auctionId;
    private final long timestamp;
    private final double bidAmount; // Only used in "PLACE_BID"


    public Transaction(PublicKey senderPublicKey, TransactionType type, String auctionId, double bidAmount, long timestamp) {
        this.senderPublicKey = senderPublicKey;
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
        return Base64.getEncoder().encodeToString(
                (senderPublicKey.toString() + type + auctionId + bidAmount + timestamp).getBytes());
    }


    @Override
    public String toString() {
        return "\n\tTransaction Details:\n" +
                "\t\tTransaction Id: " + transactionId + "\n" +
                "\t\tAuction Id: " + auctionId + "\n" +
                "\t\tSender: " + senderPublicKey.toString() + "\n" +
                "\t\tTransaction type: " + type + "\n" +
                "\t\tBid ammount:  " + bidAmount + "\n" +
                "\t\tTimestamp: " + Utils.convertTime(timestamp) + "\n";
    }

    /*Getter's*/
    public String getTransactionId() {
        return transactionId;
    }

    public PublicKey getSenderPublicKey() {
        return senderPublicKey;
    }

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
