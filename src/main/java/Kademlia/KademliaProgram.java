package Kademlia;

import java.util.Arrays;

/**
 * Class that is used to test the Kademlia Package
 */
public class KademliaProgram {

    public static void main(String[] args) {
        Node n1 = new Node("localhost",1234,true);
        Node n2 = new Node("localhost",4321,true);
        Node n3 = new Node("localhost",5050,true);
        Node n4 = new Node("localhost",8080,true);

        //System.out.println("[N1-before adding] " + n1.getRoutingTable());
        //System.out.println("=========================================");
        n1.getRoutingTable().addNodeToBucketList(n2);
        //n1.getRoutingTable().addNodeToBucketList(n3);
        n1.getRoutingTable().addNodeToBucketList(n4);
        //System.out.println("=========================================");
        //System.out.println("[N1-after adding] " + n1.getRoutingTable());
        //System.out.println("[N1-before removing] " + n1.getRoutingTable());
        //System.out.println(n1.getRoutingTable().removeNodeFromBucketList(n2));
        //System.out.println(n1.getRoutingTable().removeNodeFromBucketList(n3));
        //System.out.println("[N1-after removing] " + n1.getRoutingTable());


        System.out.println(
                n1.getRoutingTable().getClosestNodes(Constants.MAX_RETURN_FIND_NODES,n3.getNodeId())
        );


        System.out.println(n1.getRoutingTable().getClosestNodes(Constants.MAX_RETURN_FIND_NODES,n3.getNodeId()).contains(n2));
    }
}
