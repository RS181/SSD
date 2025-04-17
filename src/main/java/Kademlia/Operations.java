package Kademlia;

import BlockChain.Block;
import BlockChain.Miner;
import Cryptography.CryptoUtils;
import P2P.PeerComunication;
import P2P.Server;

import java.security.PublicKey;
import java.util.*;
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
    public static boolean ping(Node senderNode,Node targetNode,Miner miner){
        String targetHost = targetNode.getIpAddr();
        int targerPort = targetNode.getPort();
        SecureMessage secureMessage =
                new SecureMessage("PING",senderNode,miner.getPublicKey(),miner.getPrivateKey());

        String response =
                (String) PeerComunication.sendMessageToPeer(targetHost,targerPort,"PING",secureMessage);
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
                    instanceof SecureMessage routingSecureMessage
                            && routingSecureMessage.verifySignature()
                            && routingSecureMessage.getPayload() instanceof  RoutingTable senderRoutingTable
                            && checkNodeId(routingSecureMessage,senderNode)
                )
            {
                System.out.println("=====FIND_NODE Iteration [" + i + "]=====");
                List<Node> senderClosestNodes =
                        senderRoutingTable.getClosestNodes(Constants.MAX_ROUTING_FIND_NODES, targetNodeId);
                newNodes = 0;

                // Get the closes k Nodes to our target Id
                for (Node n : senderClosestNodes) {

                    if (PeerComunication.sendMessageToPeer(n.getIpAddr(), n.getPort(), "FIND_NODE", targetNodeId)
                            instanceof SecureMessage findSecureMessage &&
                            findSecureMessage.verifySignature() &&
                            checkNodeId(findSecureMessage,n)
                    )
                    {
                        List<Node> kClosestNodes = (List<Node>) findSecureMessage.getPayload();
                        for (Node closestNode : kClosestNodes) {
                            if (!closestNodesToTarget.contains(closestNode)) {
                              closestNodesToTarget.add(closestNode);
                               newNodes++;
                            }
                        }
                    } else {
                        System.out.println("Error ocurred in findNode (could be because" +
                                "of message signature or comunication error) ");
                    }
                }
                System.out.println("Closest Nodes to [" + targetNodeId + "] =" + senderClosestNodes);

                // If we didn't discover any new nodes we leave the while loop
                if (newNodes == 0)
                    break;
                i++;
                // Update the Sender's node routing table and list of neighbours
                updatePeerRoutingInfo(senderNode, closestNodesToTarget);
                System.out.println("================================");
            }else {
                System.out.println("ERROR: findNode");
                return;
            }
        }
        System.out.println("================================");
    }

    /**
     * Allows a node {@code joiningNode} to join a Kademlia network using a known {@code bootstrap} node.
     *
     * <p>The joining process involves three main steps:
     * <ol>
     *   <li>Query the bootstrap node to retrieve the {@code k} closest nodes to {@code joiningNode},
     *       and update the {@code joiningNode}'s routing table with those nodes.</li>
     *   <li>Add {@code joiningNode} to the bootstrap node's routing table.</li>
     *   <li>Notify all nodes returned by the bootstrap node of the new {@code joiningNode},
     *       allowing them to update their routing tables accordingly.</li>
     * </ol>
     * </p>
     *
     * <p>Additionally, the {@code joiningNode} retrieves the local storage from the bootstrap node
     * to initialize its own storage.
     * </p>
     *
     * @param joiningNode the node that wants to join the network
     * @param bootstrap the bootstrap node already present in the network
     *
     * @implNote The value of {@code k} (the number of closest nodes to retrieve) should be sufficiently large
     *           to ensure the joining node is properly advertised to all relevant peers.
     */
    public static void joinNetwork(Node joiningNode, Node bootstrap){
        System.out.println("=====JOIN NETWORK Iteration [0]=====");
        if (PeerComunication.sendMessageToPeer(bootstrap.getIpAddr(), bootstrap.getPort(), "FIND_NODE", joiningNode.getNodeId())
                instanceof SecureMessage findSecureMessage &&
                findSecureMessage.verifySignature() &&
                checkNodeId(findSecureMessage,bootstrap)
        ) {

            List<Node> kClosestNodes = (List<Node>) findSecureMessage.getPayload();
            updatePeerRoutingInfo(joiningNode, kClosestNodes);

            if (PeerComunication.sendMessageToPeer(
                    bootstrap.getIpAddr(), bootstrap.getPort(), "GET_STORAGE", null)
                    instanceof SecureMessage storageSecureMessage
                    && storageSecureMessage.verifySignature()
                    && checkNodeId(storageSecureMessage,bootstrap)
            ) {

                // 2. update joining nodes local storage with local storage of k closest nodes
                Map<String, Block> bootstrapStorage = (Map<String, Block>) storageSecureMessage.getPayload();

                updatePeerStorageInfo(joiningNode, bootstrapStorage);

                PeerComunication.sendMessageToPeer(
                        bootstrap.getIpAddr(), bootstrap.getPort(), "ADD_PEER", joiningNode);
                System.out.println("Closest Nodes to [" + joiningNode + "] =" + kClosestNodes);
                System.out.println("====================================");

                System.out.println("=====JOIN NETWORK Iteration [1]=====");
                for (Node n : kClosestNodes)
                    updatePeerRoutingInfo(n, new ArrayList<>(Arrays.asList(joiningNode)));
                System.out.println("====================================");
            }
            else {
                System.out.println("[GET_STORAGE] Error ocurred in joinNetwork (could be because" +
                        "of message signature or comunication error) ");
            }
        }else{
            System.out.println("[FIND_NODE] Error ocurred in joinNetwork (could be because" +
                    "of message signature or comunication error) ");
        }
    }

    /**
     * Performs the FIND_VALUE operation in the Kademlia network.
     * <p>
     * This method attempts to locate the value (a {@link Block}) associated with the provided key.
     * It starts by generating the key ID (based on the key string), and then uses the sender node's
     * routing table to find the closest known nodes to that key.
     * </p>
     *
     * <p>
     * The method iteratively queries unqueried closest nodes, asking each one for the value.
     * If a node returns the value, it is immediately returned and also stored locally at the sender node
     * for future lookups. If the value is not found, and no new nodes are discovered, the search ends
     * unsuccessfully.
     * </p>
     *
     * <p>
     * This implementation assumes a broadcast-style network where the initial routing table
     * returns all nodes in the network, as per a simplified Kademlia variant.
     * </p>
     *
     * @param senderNode The node initiating the FIND_VALUE operation.
     * @param key        The key associated with the value being searched.
     * @return The {@link Block} value if found, or {@code null} if not found in the network.
     */
    public static Block findValue(Node senderNode, String key,Miner miner) {
        String keyId = generateKeyId(key);
        Set<Node> queriedNodes = new HashSet<>();
        Set<Node> discoveredNodes = new HashSet<>();
        int i = 1;

        while (true) {
            if (
                    PeerComunication.sendMessageToPeer(
                            senderNode.getIpAddr(), senderNode.getPort(), "GET_ROUTING_TABLE", null
                    ) instanceof SecureMessage routingSecureMessage
                    && routingSecureMessage.verifySignature()
                    && routingSecureMessage.getPayload() instanceof RoutingTable senderRoutingTable
                    && checkNodeId(routingSecureMessage,senderNode)

            ) {
                System.out.println("===== FIND_VALUE Iteration [" + i + "] =====");

                List<Node> toQuery = senderRoutingTable.getClosestNodes(Constants.MAX_RETURN_FIND_NODES, keyId);
                int newNodes = 0;

                for (Node node : toQuery) {
                    if (!queriedNodes.contains(node)) {
                        queriedNodes.add(node);

                        Object response = PeerComunication.sendMessageToPeer(node.getIpAddr(), node.getPort(),
                                "FIND_VALUE", keyId);

                        if (response instanceof SecureMessage secureMessage
                                && secureMessage.verifySignature()
                                && checkNodeId(secureMessage,node)
                        )
                        {
                            if(secureMessage.getPayload() instanceof Block foundValue &&
                                    foundValue != null) {
                                System.out.printf("Value found in [%s,%s,%s]\n"
                                        , node.getNodeId(), node.getIpAddr(), node.getPort());
                                // Tries to add to sender local storage
                                Operations.store(senderNode, key, foundValue,miner);


                                System.out.println("==================================");
                                return foundValue;
                            }else if (secureMessage.getPayload() instanceof List<?> returnedNodes) {
                                for (Object obj : returnedNodes) {
                                    if (obj instanceof Node n && !discoveredNodes.contains(n)) {
                                        discoveredNodes.add(n);
                                        newNodes++;
                                    }
                                }
                            }
                        } else {
                            System.out.println("Error ocurred in findValue (could be because" +
                                    "of message signature or comunication error) ");
                        }
                    }
                }
                //System.out.println("==> " + discoveredNodes);

                if (newNodes == 0) {
                    System.out.println("Value ["+ keyId + "] not found");
                    System.out.println("==================================");
                    return null;
                }

                updatePeerRoutingInfo(senderNode, new ArrayList<>(discoveredNodes));
                System.out.println("==================================");
                i++;
            } else {
                System.out.println("Error while trying to obtain Secure message.");
                break;
            }
        }
        return null;
    }

    /**
     * Stores a (key, value) pair in the Kademlia network.
     * <p>
     * This method is used to distribute and replicate a value (e.g., a block)
     * across the k nodes that are closest to the key in the XOR metric space.
     * The key is hashed to generate a key ID, which is used to determine where
     * the value should be stored in the network.
     * </p>
     *
     * @param senderNode The node initiating the STORE operation.
     * @param key        The string key to be used for identifying the value (in this case the blockhash).
     * @param value      The value (a Block) to be stored in the network.
     *
     * @note This implementation follows a broadcast-style STORE. We assume that the {@code FIND_NODE}
     *       performed from the senderNode returns all nodes in the network (i.e., the {@code kClosestNodes}
     *       actually correspond to all active Kademlia nodes). This guarantees that the value is propagated
     *       across the entire network.
     */
    public static void store (Node senderNode, String key, Block value, Miner miner){
        String keyId =  generateKeyId(key);
        System.out.println("Key Id = " + keyId);

        // Get the k closest Nodes  to keyId (from sender Node)

        if (
                PeerComunication.sendMessageToPeer(
                        senderNode.getIpAddr(), senderNode.getPort(),
                        "FIND_NODE",keyId
                ) instanceof  SecureMessage findSecureMessage
                && findSecureMessage.verifySignature()
                && checkNodeId(findSecureMessage,senderNode)
        ){
            List<Node> kClosestNodes = (List<Node>) findSecureMessage.getPayload();



            // Wraps the Key and Block in a  wrapper class (a.k.a. BlockKeyWrapper)
            BlockKeyWrapper blockKeyWrapper = new BlockKeyWrapper(keyId,value);

            // Store the <Key,Value> pair in those KClosestNodes
            for (Node n : kClosestNodes){
                SecureMessage storeSecureMessage =
                        new SecureMessage("STORE",blockKeyWrapper,
                                miner.getPublicKey(),miner.getPrivateKey());

                //PeerComunication.sendMessageToPeer(
                //        n.getIpAddr(),n.getPort(),"STORE",blockKeyWrapper
                //);
                PeerComunication.sendMessageToPeer(
                        n.getIpAddr(),n.getPort(),"STORE",storeSecureMessage
                );
            }

        }else {
            System.out.println("Error ocurred in STORE (could be because" +
                    "of message signature or comunication error) ");
        }

    }

    /**
     * Updates the routing table and list of known Neighbours of a certain Peer
     *
     * @param targetNode           Node that we are going to send a list of Nodes to update
     *                             its routing table and list of known Neighbours
     * @param closestNodesToTarget list of Nodes we are going to send
     */
    private static void updatePeerRoutingInfo(Node targetNode, List<Node> closestNodesToTarget) {
        for (Node n : closestNodesToTarget){
            System.out.println(
                    PeerComunication.sendMessageToPeer(
                            targetNode.getIpAddr(), targetNode.getPort(),"ADD_PEER",n
                    )
            );
        }
    }

    /**
     * Updates the local Storage of a certain Peer
     *
     * @param targetNode Node that we are going to send a Block's to update
     *                   local storage
     * @param storage    Storage tha contains all the block we are going to send targetNode
     */
    private  static void updatePeerStorageInfo(Node targetNode, Map<String, Block> storage){
        for (Block b : storage.values()){
            System.out.println(
                    PeerComunication.sendMessageToPeer(
                            targetNode.getIpAddr(), targetNode.getPort(), "ADD_TO_STORAGE",b
                    )
            );
        }
    }

    public static boolean checkNodeId(SecureMessage secureMessage, Node n) {
        PublicKey pubKey = secureMessage.getSenderPublickKey();
        String claimedNodeId = n.getNodeId();
        String computedNodeId = CryptoUtils.generateSecureNodeId(pubKey);
        System.out.println("claimedNodeId = " + claimedNodeId );
        System.out.println("computedNodeId = " + computedNodeId);
        System.out.println("Node id is valid? : " + claimedNodeId.equals(computedNodeId));
        return claimedNodeId.equals(computedNodeId);
    }

    /**
     * Generates a key ID based on a certain key.
     * The key ID is defined as the first 8 bits of the SHA-1 hash of the key,
     * returned as an 8-character binary string (e.g., "00110001").
     *
     * TODO: possivelmente vamos ter que ajustar para mais bits
     * The first byte of the hash is extracted and converted into an 8-bit binary string.
     *
     * @param key Strin that corresponds to a key
     * @return The generated Key ID as an 8-bit binary string.
     */
    public static String generateKeyId(String key) {
        String input = CryptoUtils.getHash1(key);
        byte[] bytes = input.getBytes();
        int first8Bits = bytes[0] & 0xFF; // Positive 8-bit value
        return String.format("%8s", Integer.toBinaryString(first8Bits)).replace(' ', '0'); // 8-bit binary
    }
}
