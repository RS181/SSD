package Kademlia;

import java.util.List;

/**
 * Class that represents a bucket a routing table of a Kademlia node
 */
public class Bucket {

    String prefix; // Prefix of 0's of the bucket (represents the corresponding tree prefix of this bucket, see image to understand)
    int k; // Max number of nodes per bucket
    List<Node> nodeList; // List of Nodes in current bucket

}
