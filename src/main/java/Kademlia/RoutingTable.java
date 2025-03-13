package Kademlia;

import java.util.List;

/**
 * Class that Represents a Routing table of a kademlia node
 *
 * TODO: confirmar que Routing table de um nó corresponde as distancias minima deste no para outros
 */
public class RoutingTable {


    int lengthOfNodeId; // determines the amount of buckets in the routing table

    List<Bucket> bucketList; // List of buckets (each Bucket contains a list of Node's)
}
