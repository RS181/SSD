package Kademlia;

import java.util.Arrays;

/**
 * Class that is used to test the Kademlia Package
 */
public class KademliaProgram {

    public static void main(String[] args) {
        Node n1 = new Node("localhost",1234);

        System.out.println(n1.getNodeId());

    }
}
