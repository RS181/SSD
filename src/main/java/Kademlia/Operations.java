package Kademlia;

import BlockChain.Block;
import P2P.PeerComunication;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents comunication operations between kademlia nodes in a kademlia network
 */
public class Operations {
    private static final Logger logger = Logger.getLogger(Operations.class.getName());

    /**
     * Enum for message types used in Kademlia.
     */
    public enum MessageType {
        PING, FIND_NODE, FIND_VALUE, STORE
    }

    public static boolean ping(Node senderNode,Node targetNode){
        String targetHost = targetNode.getIpAddr();
        int targerPort = targetNode.getPort();
        String response = (String) PeerComunication.sendMessageToPeer(targetHost,targerPort,"PING",senderNode);
        if (response != null)
            return true;
        return false;
    }

    public static void findNode(Node senderNode,RoutingTable senderRoutingTable, String targetNodeId){
        // Contains all nodes received in FIND_NODE response
        List<Node> closestNodesToTarget = new ArrayList<>();
        int newNodes;
        int i = 1;

        // Do the FIND_NODE iteration process until no new nodes are discovered
        while (true) {
            List<Node> senderClosestNodes = senderRoutingTable.getClosestNodes(Constants.MAX_ROUTING_FIND_NODES,targetNodeId);
            System.out.println("=====FIND_NODE Iteration ["+i+"]=====");
            System.out.println("Closest Nodes to [" +targetNodeId +"] =" + senderClosestNodes);

            newNodes = 0;
            for (Node n : senderClosestNodes) {
                List<Node> kClosestNodes =
                        (List<Node>) PeerComunication.sendMessageToPeer(n.getIpAddr(), n.getPort(), "FIND_NODE", targetNodeId);

                for (Node closestNode : kClosestNodes) {
                    if (!closestNodesToTarget.contains(closestNode)) {
                        closestNodesToTarget.add(closestNode);
                        newNodes++;
                    }
                }
            }

            // If we didn't discover any new nodes we leave the shile loop
            if (newNodes ==0)
                break;
            i++;

            // Update the Sender's node routing table and list of neighbours
            updateSenderNode(senderNode,closestNodesToTarget);
            System.out.println("================================");
        }
        System.out.println("================================");

    }

    /**
     * Updates the routing table and list of knownNeighbours of a certain Peer
     * @param senderNode Node that we are going to send a list of Nodes
     * @param closestNodesToTarget list of Nodes we are going to send
     */
    private static void updateSenderNode(Node senderNode, List<Node> closestNodesToTarget) {
        for (Node n : closestNodesToTarget){
            System.out.println(
                    PeerComunication.sendMessageToPeer(
                            senderNode.getIpAddr(),senderNode.getPort(),"ADD_PEER",n
                    )
            );
        }

    }

    public static void findValue(Node senderNode, String key){
        //TODO
    }

    public static void store (Node senderNode, String key, Block value){

    }
}
