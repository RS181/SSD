package Kademlia;

import BlockChain.Block;
import P2P.PeerComunication;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * @param senderNode
     * @param targetNodeId
     */
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
        System.out.println("=====JOIN NETWORK Iteration [0]=====");
        // Firstly we get the K closest Nodes to joining Node and update
        // the joining Node's routing table and list of neighbours
        // Note: We have to guarante tha k is large enough, so that
        // we receive all nodes the bootstrap nodes knows, so that we
        // can send ADD_PEER(joiningNode) to all of them (i.e. we
        // broadcast the joining node to all peer's the bootstrap nodes
        // knows)
        List<Node> kClosestNodes = (List<Node>) PeerComunication.sendMessageToPeer(
                        bootstrap.getIpAddr(), bootstrap.getPort(), "FIND_NODE", joiningNode.getNodeId());
        updatePeer(joiningNode,kClosestNodes);

        // Seconly we add the joining Node to bootstrap's routing table and list of neighbours
        PeerComunication.sendMessageToPeer(
                bootstrap.getIpAddr(),bootstrap.getPort(),"ADD_PEER",joiningNode);
        System.out.println("Closest Nodes to [" +joiningNode +"] =" + kClosestNodes);
        System.out.println("====================================");

        //Finaly broadcast ADD_PEER, to add joining Node to all
        // the nodes the bootstrap node Knows (in principle all
        // the nodes that are part of the network)
        System.out.println("=====JOIN NETWORK Iteration [1]=====");

        for( Node n : kClosestNodes)
            updatePeer(n,new ArrayList<>(Arrays.asList(joiningNode)));
        System.out.println("====================================");

    }

    /**
     * Updates the routing table and list of known Neighbours of a certain Peer
     * @param senderNode Node that we are going to send a list of Nodes to update
     *                   its routing table and list of known Neighbours
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
