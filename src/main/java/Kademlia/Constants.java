package Kademlia;

/**
 * Class that has some constant's relating to Kademlia
 */
public class Constants {

    /**
     * Defines the max number of Nodes in a bucket (a.k.a K)
     */
    public static final int MAX_NUMBER_NODES_BUCKET = 10;

    /**
     * Defines the number of Nodes returned by FIND operation
     * NOTE: In this case we can adjust this constant so that
     * when we receive the nodes of a FIND operation, we try
     * to receive all nodes that are present in the network
     * , so that all nodes know eachother (a.k.a all nodes
     * are bootstrap nodes)
     */
    public static final int MAX_RETURN_FIND_NODES = 4;

    /**
     * Defines the number of Nodes, in the Routing table,
     * we send FIND operation (a.k.a alpha)
     * NOTE: here we define a smaller value, so that we can
     * see the iteration process associated with FIND
     * operation
     */
    public static final int MAX_ROUTING_FIND_NODES = 3;
}
