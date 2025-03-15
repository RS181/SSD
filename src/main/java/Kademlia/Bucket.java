package Kademlia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that represents a bucket a routing table of a Kademlia node
 */
public class Bucket {

    private String ownerNodeId; // Node Id of the owner of this bucket
    private String prefix; // Prefix of 0's of the bucket (represents the corresponding tree prefix of this bucket, see image to understand)
    private int k; // Max number of nodes per bucket
    private LinkedList<Node> nodeList = new LinkedList<>(); // List of Nodes in current bucket

    /**
     *
     * @param prefix
     * @param k
     */
    public Bucket(String ownerNodeId, String prefix, int k) {
        this.ownerNodeId = ownerNodeId;
        this.prefix = prefix;
        this.k = k;
        nodeList = new LinkedList<>();
    }

    /* Getter's */
    public String getOwnerNodeId(){return ownerNodeId; }
    public String getPrefix() {
        return prefix;
    }

    public int getK() {
        return k;
    }

    public LinkedList<Node> getNodeList() {
        return nodeList;
    }

    /**
     *
     * @param n
     * @return
     */
    public boolean addNode(Node n){
        if (nodeList.size() > k) {
            System.out.println("Bucket '" + prefix + "' está cheio!!");
            return false;
        }
        else {
            nodeList.addLast(n);
            return true;
        }

    }



    @Override
    public String toString() {
        return "[prefix = " + this.prefix + ", " +
                "Nodes in bucket = " + nodeList + "]";
    }


}
