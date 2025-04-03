package Kademlia;

/**
 * Class that has some constant's relating to Kademlia
 */
public class Constants {

    /**
     * Defines the max number of Nodes in a bucket (a.k.a K)
     */
    public static final int MAX_NUMBER_NODES_BUCKET = 2;

    /**
     * Defines the number of Nodes returned by FIND operation
     */
    public static final int MAX_RETURN_FIND_NODES = 2;

    /**
     * Defines the number of Nodes, in the Routing table, we send FIND operation (a.k.a alpha)
     */
    public static final int MAX_ROUTING_FIND_NODES = 3;
}
