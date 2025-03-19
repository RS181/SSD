package Kademlia;

import java.util.Arrays;

/**
 * Class that is used to test the Kademlia Package
 */
public class KademliaProgram {

    public static void main(String[] args) {
        Node n1 = new Node("localhost",1234,true);

        System.out.print(n1.getRoutingTable());

        Node n2 = new Node("localhost",4321,true);

        n1.getRoutingTable().addNodeToBucketList(n2);
        System.out.print(n1.getRoutingTable());

    }
}
