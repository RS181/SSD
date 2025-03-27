package Kademlia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that Represents a Routing table of a kademlia node
 *
 */
public class RoutingTable implements Serializable {
    // Info of owner node of this routing table
    String nodeId;
    String ipAddr;
    int port;

    int lengthOfNodeId; // determines the amount of buckets in the routing table
    List<Bucket> bucketList; // List of buckets (each Bucket contains a list of Node's)

    RoutingTable(String nodeId,String ipAddr, int port){
        this.nodeId = nodeId;
        this.ipAddr = ipAddr;
        this.port = port;
        this.lengthOfNodeId = nodeId.length();
        bucketList = new ArrayList<>(lengthOfNodeId);
        initializeBucketList(lengthOfNodeId);
    }


    /**
     * Initializes the bucket list with given number of bucket
     * @param numberBuckets that are going to be created
     */
    private void initializeBucketList(int numberBuckets){
        for (int i = 0 ; i <= numberBuckets ; i++){
            char[] c = new char[i];
            Arrays.fill(c, '0');
            String prefix = new String(c);
            Bucket b = new Bucket(nodeId,prefix,Constants.MAX_NUMBER_NODES);
            // Add owner node of this routing table to respetiv bucket (prefix all 0's)
            if(i == numberBuckets)
                b.addNode(new Node(this.ipAddr,port,false));
            //System.out.println(b);
            bucketList.add(b);
        }

    }

    /**
     * Retrieves the bucket from the bucket list that has the longest common prefix
     * with the given node ID.
     *
     * The method determines the longest prefix of consecutive '0's in the node ID
     * and searches for a bucke with the same prefix.
     *
     * @param targetNodeId The ID of the node for which the closest bucket is sought.
     * @return The bucket with the longest common prefix matching the node ID, or `null` if no such bucket exists.
     */
    public Bucket getClosestBucket(String targetNodeId){
        // Tenho que fazer XOR antes de procurar pelo bucket

        String distanceTarget = calculateDistance(targetNodeId);
        System.out.println(nodeId + " XOR " + targetNodeId + " = " + distanceTarget );
        String prefix = "";
        for (int i = 0 ; i < distanceTarget.length() ; i++){
            if (distanceTarget.charAt(i) == '0')
                prefix += "0";
            else
                break;
        }
        for (Bucket b : bucketList){
            if (prefix.equals(b.getPrefix()))
                return b;
        }
        System.out.println("Bucket prefix = " + prefix);
        return null;
    }

    /**
     * Add's a node to the 'closest' bucket
     * @param n
     * @return
     */
    public  boolean addNodeToBucketList(Node n){
        Bucket closestBucket = getClosestBucket(n.getNodeId());

        if (closestBucket == null)  return false;

        return closestBucket.addNode(n);
    }

    public boolean removeNodeFromBucketList(Node n){
        Bucket closestBucket = getClosestBucket(n.getNodeId());
        if (closestBucket == null) return false;

        return closestBucket.removeNode(n);
    }


    /**
     * Calculates XOR between this node Id and the node id of target
     * @return the String that corresponds to the XOR of this node Id and target node Id
     */
    private String calculateDistance(String targetNodeId){
        StringBuilder result = new StringBuilder();
        for (int i = 0 ; i < nodeId.length() ; i++){
            if (nodeId.charAt(i) == targetNodeId.charAt(i))
                result.append("0");
            else
                result.append("1");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        ans.append("Routing table of "+nodeId+":\n");
        for (Bucket b : bucketList)
            ans.append("   " + b.toString() + "\n");
        return ans.toString();
    }
}
