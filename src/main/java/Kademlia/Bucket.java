package Kademlia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that represents a bucket in a routing table of a Kademlia node
 */
public class Bucket implements Serializable {
    private String ownerNodeId;         // Node Id of the owner of this bucket
    private String prefix;              // Prefix of 0's of the bucket (represents the corresponding tree prefix of this bucket, see image on notes to understand)
    private int k;                      // Max number of nodes per bucket
    private LinkedList<Node> nodeList;  // List of Nodes in current bucket

    /**
     * Constructor for a Bucket
     *
     * @param ownerNodeId Node If of owner of this bucket
     * @param prefix      Prefix of 0's of the bucket
     * @param k           Max number of nodes per bucket
     */
    public Bucket(String ownerNodeId, String prefix, int k) {
        this.ownerNodeId = ownerNodeId;
        this.prefix = prefix;
        this.k = k;
        nodeList = new LinkedList<>();
    }

    /* Getters */
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

    /* Auxiliar methods */

    /**
     * Add's a node to the bucket list
     *
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
     *
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

    /**
     * @return number of nodes that are currenltly in the bucket
     */
    public  int size(){
        return nodeList.size();
    }

    @Override
    public String toString() {
        return "[prefix = " + this.prefix + ", " +
                "Nodes in bucket = " + nodeList + "]";
    }
}
