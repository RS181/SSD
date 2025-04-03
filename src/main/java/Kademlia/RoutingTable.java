package Kademlia;

import java.io.Serializable;
import java.util.*;

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
            Bucket b = new Bucket(nodeId,prefix,Constants.MAX_NUMBER_NODES_BUCKET);
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
     * Returns a List of Closest Nodes of given target Node id

     * @param alpha the size of the list we are going to return
     * @param targeNodeId the node we are trying to find the closest Nodes
     * @return List of Closest Nodes of given target Node id
     */
    public List<Node> getClosestNodes(int alpha,String targeNodeId){
        List<Node> aux = new ArrayList<>();
        List<Node> ans = new ArrayList<>();

        // First we get a list of all Nodes in the routing table
        for (int i = 0 ; i < bucketList.size() ; i++){
            Bucket bucket = bucketList.get(i);
            List<Node> nodeList = bucket.getNodeList();
            for (int j = 0 ; j < nodeList.size() ; j++){
                aux.add(nodeList.get(j));
            }
        }

        // Secondly we create a Map i n wich the key is the Node and
        // the value is the  Xor distance in decimal of current
        // Node and target Node
        Map<Node,Integer> m = new HashMap<>();
        for (int i = 0 ; i < aux.size() ; i++){
            Node current = aux.get(i);
            String xorDistance = calculateDistance(current.getNodeId(),targeNodeId);
            //System.out.println(current + " XOR " + targeNodeId + " = " + xorDistance);
            m.put(current, binaryToDecimal(xorDistance));
        }

        // Lastly we return the alpha Nodes in the sorted Map with lower correspondent value
        Map<Node,Integer> sortedMap = sortByValueAscending(m);
        //System.out.println(sortedMap);
        for (Node n : sortedMap.keySet()){
            if (ans.size() == alpha)
                break;
            ans.add(n);
        }
        return ans;
    }

    /**
     * Add's a node to the 'closest' bucket
     * @param n node we are trying to add
     * @return True if node was added and False otherwise
     */
    public  boolean addNodeToBucketList(Node n){
        Bucket closestBucket = getClosestBucket(n.getNodeId());

        if (closestBucket == null)  return false;

        // To avoid duplicates Nodes in the same Bucket
        for(Node node : closestBucket.getNodeList()) {
            if (node.equals(n))
                return false;
        }
        return closestBucket.addNode(n);
    }

    /**
     * Removes a node from its respectiv bucket
     * @param n node we are trying to remove
     * @return True if node was removed and False otherwise
     */
    public boolean removeNodeFromBucketList(Node n){
        Bucket closestBucket = getClosestBucket(n.getNodeId());
        if (closestBucket == null) return false;

        return closestBucket.removeNode(n);
    }


    /**
     * Calculates XOR between this node Id and the node id of target
     * @param targetNodeId node id of target
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

    /**
     * Calculate XOR between two given nodes
     * @param nodeId1
     * @param nodeId2
     * @return the String that corresponds to the XOR of nodeId1 and nodeId2
     */
    private String calculateDistance(String nodeId1, String nodeId2){
        if (nodeId1.length() != nodeId2.length())
            return  null;
        StringBuilder result = new StringBuilder();
        for (int i = 0 ; i < nodeId1.length() ; i++){
            if (nodeId1.charAt(i) == nodeId2.charAt(i))
                result.append("0");
            else
                result.append("1");
        }
        return result.toString();
    }

    public  int binaryToDecimal(String binaryString) {
        return Integer.parseInt(binaryString, 2);
    }


    /**
     * Method that receives a map and returns a map that is sorted by Ascending order
     * of value
     * @param map Map that we are going to sort by value
     * @return the sorted map (by Ascending order of value)
     */
    private Map<Node, Integer> sortByValueAscending(Map<Node, Integer> map) {
        // Convert a map to a list of entries
        List<Map.Entry<Node, Integer>> list = new ArrayList<>(map.entrySet());

        // order the list based on the values (Ascending)
        list.sort(Map.Entry.comparingByValue());

        // Creates a LinkedHashMap to mantain the orderded list
        Map<Node, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Node, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
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
