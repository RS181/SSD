package Kademlia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that represents a bucket a routing table of a Kademlia node
 */
public class Bucket implements Serializable {

    private String ownerNodeId; // Node Id of the owner of this bucket
    private String prefix; // Prefix of 0's of the bucket (represents the corresponding tree prefix of this bucket, see image to understand)
    private int k; // Max number of nodes per bucket
    private LinkedList<Node> nodeList; // List of Nodes in current bucket

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
     * Add's a node to the bucket list
     * @param n node that we are trying to add
     * @return True if node was added and False otherwise
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

    /**
     * Removes a node from the bucket list
     * @param n node that we are trying to remove
     * @return True if node was removed and False otherwise
     */
    public boolean removeNode(Node n){
        for (Node node : nodeList){
            if(node.equals(n)) {
                nodeList.remove(node);
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "[prefix = " + this.prefix + ", " +
                "Nodes in bucket = " + nodeList + "]";
    }


}
