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

    /**
     * Sends a {@code PING} request from the {@code senderNode} to the {@code targetNode} to check if it is alive.
     *
     * <p>This method is used in Kademlia to verify the liveness of a node and ensure it is still reachable.
     * The target node should respond with any non-null response if it is active. If no response is received,
     * the node is considered unreachable and may eventually be removed from the routing table.
     *
     * @param senderNode the node initiating the PING
     * @param targetNode the node being pinged
     * @return {@code true} if a response is received (node is alive), {@code false} otherwise
     */
    public static boolean ping(Node senderNode,Node targetNode){
        String targetHost = targetNode.getIpAddr();
        int targerPort = targetNode.getPort();
        String response = (String) PeerComunication.sendMessageToPeer(targetHost,targerPort,"PING",senderNode);
        if (response != null)
            return true;
        return false;
    }

    /**
     * Performs the Kademlia {@code FIND_NODE} operation starting from a given {@code senderNode}
     * and searching for nodes close to the {@code targetNodeId}.
     *
     * <p>The algorithm iteratively queries the routing tables of the closest known nodes to find
     * the {@code k} nodes whose IDs are closest to the target. This process continues until
     * no new closer nodes are discovered in an iteration.
     *
     * <p>Steps involved:
     * <ol>
     *   <li>Query the sender's routing table for the closest nodes to {@code targetNodeId}.
     *   <li>Send {@code FIND_NODE} messages to those nodes and collect their responses.
     *   <li>Repeat the process with any new nodes discovered, updating the sender's routing table.
     *   <li>Terminate when no new nodes are found in an iteration.
     * </ol>
     *
     * @param senderNode the node initiating the FIND_NODE lookup
     * @param targetNodeId the ID of the node we are trying to locate
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

    /**
     * Allows a node {@code joiningNode} to join a Kademlia network using a known {@code bootstrap} node.
     *
     * <p>The process involves three main steps:
     * <ol>
     *   <li>Query the bootstrap node to retrieve the {@code k} closest nodes to {@code joiningNode},
     *       and update {@code joiningNode}'s routing table with them.
     *   <li>Add {@code joiningNode} to the bootstrap node's routing table.
     *   <li>Broadcast the presence of {@code joiningNode} to all the nodes returned by the bootstrap,
     *       so they can also update their routing tables to include the new node.
     * </ol>
     *
     * <p><strong>Note:</strong> The value of {@code k} must be large enough to ensure that all peers known
     * by the bootstrap node are included. This guarantees that the new node is properly advertised.
     *
     * @param joiningNode the node that wants to join the network
     * @param bootstrap the bootstrap node already part of the network
     */
    public static void joinNetwork(Node joiningNode, Node bootstrap){
        System.out.println("=====JOIN NETWORK Iteration [0]=====");
        List<Node> kClosestNodes = (List<Node>) PeerComunication.sendMessageToPeer(
                        bootstrap.getIpAddr(), bootstrap.getPort(), "FIND_NODE", joiningNode.getNodeId());
        updatePeer(joiningNode,kClosestNodes);

        PeerComunication.sendMessageToPeer(
                bootstrap.getIpAddr(),bootstrap.getPort(),"ADD_PEER",joiningNode);
        System.out.println("Closest Nodes to [" +joiningNode +"] =" + kClosestNodes);
        System.out.println("====================================");

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
