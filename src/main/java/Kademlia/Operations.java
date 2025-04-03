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

    public static void findNode(Node senderNode, String targetNodeId){
        // Contains all nodes received in FIND_NODE response
        List<Node> closestNodesToTarget = new ArrayList<>();
        int newNodes;
        int i = 1;

        // Do the FIND_NODE iteration process until no new nodes are discovered
        while (true) {
            if (
                    PeerComunication.sendMessageToPeer(
                            senderNode.getIpAddr(),senderNode.getPort(),
                            "GET_ROUTING_TABLE",null)
                    instanceof RoutingTable senderRoutingTable
                )
            {
                System.out.println("=====FIND_NODE Iteration [" + i + "]=====");
                List<Node> senderClosestNodes = senderRoutingTable.getClosestNodes(Constants.MAX_ROUTING_FIND_NODES, targetNodeId);
                newNodes = 0;

                // Get the closes k Nodes to our target Id
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
                System.out.println("Closest Nodes to [" + targetNodeId + "] =" + senderClosestNodes);

                // If we didn't discover any new nodes we leave the while loop
                if (newNodes == 0)
                    break;
                i++;
                // Update the Sender's node routing table and list of neighbours
                updatePeer(senderNode, closestNodesToTarget);
                System.out.println("================================");
            }else {
                System.out.println("ERRO: findNode");
                return;
            }
        }
        System.out.println("================================");
    }

    // TODO: Join network ainda não esta a funcionar
    public static void joinNetwork(Node joiningNode, Node bootstrap){
        List<Node> closestNodesToTarget = new ArrayList<>();
        int newNodes;
        int i = 0;

        System.out.println("=====JOIN NETWORK Iteration ["+i+"]=====");
        // First we get the K closest Nodes to joining Node and update
        // the joining Node's routing table and list of neighbours
        List<Node> kClosestNodes = (List<Node>) PeerComunication.sendMessageToPeer(
                        bootstrap.getIpAddr(), bootstrap.getPort(), "FIND_NODE", joiningNode.getNodeId());
        updatePeer(joiningNode,kClosestNodes);

        // Add the joining Node to bootstrap's routing table and list of neighbours
        PeerComunication.sendMessageToPeer(
                bootstrap.getIpAddr(),bootstrap.getPort(),"ADD_PEER",joiningNode);
        System.out.println("Closest Nodes to [" +joiningNode +"] =" + kClosestNodes);
        System.out.println("====================================");

        i++;

        // Do the FIND_NODE iteration process until no new nodes are discovered
        while (true){
            break;
        }
    }

    /**
     * Updates the routing table and list of knownNeighbours of a certain Peer
     * @param senderNode Node that we are going to send a list of Nodes
     * @param closestNodesToTarget list of Nodes we are going to send
     */
    private static void updatePeer(Node senderNode, List<Node> closestNodesToTarget) {
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
