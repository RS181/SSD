package Kademlia;

import BlockChain.Block;
import BlockChain.Blockchain;
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
     * Sends a {@code PING} request from the {@code senderNode} to the {@code targetNode}
     * using cryptographic credentials from the specified {@code miner} to verify the target node's availability.
     *
     * <p>This method is part of the Kademlia protocol and is used to check if a node is still alive and reachable.
     * A {@code PING} message is sent containing a secure payload signed with the miner's keys.
     * If the target node responds with a non-null message, it is considered alive; otherwise, it is deemed unreachable
     * and may be subject to removal from the routing table.
     * </p>
     *
     * @param senderNode the node initiating the PING request
     * @param targetNode the node being pinged
     * @param miner      the miner whose public and private keys are used to sign the message
     * @return           {@code true} if a response is received (indicating the target node is alive),
     *                   {@code false} otherwise
     */
    public static boolean ping(Node senderNode,Node targetNode,Miner miner){
        String targetHost = targetNode.getIpAddr();
        int targerPort = targetNode.getPort();
        SecureMessage secureMessage =
                new SecureMessage("PING",senderNode,miner.getPublicKey(),miner.getPrivateKey());
        String response =
                (String) PeerComunication.sendMessageToPeer(targetHost,targerPort,"PING",secureMessage);

        return response != null;
    }

    /**
     * Executes the Kademlia {@code FIND_NODE} operation starting from the {@code senderNode},
     * aiming to locate nodes that are closest to a given {@code targetNodeId}.
     *
     * <p>This method performs an iterative lookup using the sender's routing table and recursively
     * queries known peers for nodes that are closer to the target ID. During each iteration:
     * <ol>
     *   <li>The sender node retrieves its current routing table via a secure message.
     *   <li>It selects the {@code k} closest nodes to the target ID and sends {@code FIND_NODE} requests.
     *   <li>Each response (if valid and signed correctly) returns additional nodes closer to the target.
     *   <li>The process continues with newly discovered nodes until no closer nodes are found.
     * </ol>
     *
     * <p>All discovered nodes are used to update the sender's routing table once the lookup concludes.
     * Messages are verified using cryptographic signatures to ensure integrity and authenticity.
     *
     * @param senderNode   the node initiating the {@code FIND_NODE} operation
     * @param targetNodeId the ID of the node being searched for (can be any valid Kademlia node ID)
     */
    public static void findNode(Node senderNode, String targetNodeId){
        // Contains all nodes received in FIND_NODE response
        List<Node> closestNodesToTarget = new ArrayList<>();
        int newNodes;
        int i = 1;

        String senderIp = senderNode.getIpAddr();
        int senderPort = senderNode.getPort();

        // Do the FIND_NODE iteration process until no new nodes are discovered
        while (true) {
            if (PeerComunication.sendMessageToPeer( senderIp, senderPort, "GET_ROUTING_TABLE", null )
                        instanceof SecureMessage routingSecureMessage
                && routingSecureMessage.verifySignature()
                && routingSecureMessage.getPayload() instanceof RoutingTable senderRoutingTable
                && checkNodeId( routingSecureMessage, senderNode )
                )
            {
                System.out.println("=====FIND_NODE Iteration [" + i + "]=====");
                List<Node> senderClosestNodes =
                        senderRoutingTable.getClosestNodes(Constants.MAX_ROUTING_FIND_NODES, targetNodeId);
                newNodes = 0;

                // Get the closes k Nodes to our target Id
                for (Node n : senderClosestNodes) {

                    if (PeerComunication.sendMessageToPeer(n.getIpAddr(), n.getPort(), "FIND_NODE", targetNodeId)
                            instanceof SecureMessage findSecureMessage
                        && findSecureMessage.verifySignature()
                        && checkNodeId(findSecureMessage,n)
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
     * <p>The join process follows these main steps:
     * <ol>
     *   <li>Contact the {@code bootstrap} node via {@code FIND_NODE} to retrieve the {@code k} closest nodes
     *       to the {@code joiningNode}, and update the {@code joiningNode}'s routing table.</li>
     *   <li>Synchronize local data: retrieve the bootstrap node’s storage and blockchain (checking if this information
     *   came from bootstrap node with the help of SecureMessage)</li>
     *       on the {@code joiningNode}, and apply the received data.</li>
     *   <li>Add the {@code joiningNode} to the bootstrap's routing table via {@code ADD_PEER}.</li>
     *   <li>Notify all {@code k} closest nodes about the new {@code joiningNode} to allow them to update
     *       their routing tables.</li>
     * </ol>
     *
     * <p>All communication is performed using secure messages with signature verification to ensure authenticity
     * and prevent tampering.
     *
     * @param joiningNode the node that wants to join the network
     * @param bootstrap   the bootstrap node already present in the network
     *
     * @implNote The value of {@code k} (the number of closest nodes to retrieve) should be sufficiently large
     *           to ensure the joining node is properly advertised to all relevant peers.
     */
    public static void joinNetwork(Node joiningNode, Node bootstrap){
        System.out.println("=====JOIN NETWORK Iteration [0]=====");
        String bootstrapIp = bootstrap.getIpAddr();
        int bootstrapPort = bootstrap.getPort();
        String joiningId = joiningNode.getNodeId();

        if (PeerComunication.sendMessageToPeer(bootstrapIp,bootstrapPort, "FIND_NODE", joiningId)
                instanceof SecureMessage findSecureMessage
            && findSecureMessage.verifySignature()
            && checkNodeId(findSecureMessage,bootstrap)
        ) {

            List<Node> kClosestNodes = (List<Node>) findSecureMessage.getPayload();
            updatePeerRoutingInfo(joiningNode, kClosestNodes);

            if (PeerComunication.sendMessageToPeer(bootstrapIp, bootstrapPort, "GET_STORAGE", null)
                    instanceof SecureMessage storageSecureMessage
                && storageSecureMessage.verifySignature()
                && checkNodeId(storageSecureMessage,bootstrap)
            ) {

                // Update joining nodes local storage with local storage of k closest nodes
                Map<String, Block> bootstrapStorage = (Map<String, Block>) storageSecureMessage.getPayload();
                updatePeerStorageInfo(joiningNode, bootstrapStorage);

                PeerComunication.sendMessageToPeer(bootstrapIp, bootstrapPort, "ADD_PEER", joiningNode);
                System.out.println("Closest Nodes to [" + joiningNode + "] =" + kClosestNodes);
                System.out.println("====================================");

                // Update joining node blockchain with bootstraps blockchain
                if (PeerComunication.sendMessageToPeer(bootstrapIp, bootstrapPort,"GET_BLOCKCHAIN",null)
                        instanceof  SecureMessage getBlockchainSecureMessage
                    && getBlockchainSecureMessage.verifySignature()
                    && checkNodeId(getBlockchainSecureMessage,bootstrap)
                    && getBlockchainSecureMessage.getPayload()
                            instanceof Blockchain bootstrapBlockchain
                )
                {
                    updatePeerBlockchain(joiningNode, bootstrapBlockchain);
                }

                System.out.println("=====JOIN NETWORK Iteration [2]=====");
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
     * Executes the Kademlia {@code FIND_VALUE} operation to search for a {@link Block} associated with the given {@code key}.
     *
     * <p>This method initiates the lookup from the {@code senderNode}, generating a key ID from the input key and
     * using the node's routing table to identify the closest known peers. It then iteratively queries these nodes
     * for the value:</p>
     *
     * <ol>
     *   <li>If a node responds with the associated {@link Block}, the search ends successfully and the block is returned.</li>
     *   <li>If a node instead returns a list of closer nodes, the search continues recursively with those new nodes.</li>
     *   <li>All communications are verified via secure messages with signature checking to ensure authenticity.</li>
     *   <li>If the value is found, it is also stored locally in the {@code senderNode} using the given {@code miner}'s keys.</li>
     *   <li>The process terminates when no new nodes are discovered or a value is found.</li>
     </ol>
     *
     * <p>This implementation assumes a simplified broadcast-style routing behavior where the full routing table is accessible
     * during each iteration.</p>
     *
     * @param senderNode the node initiating the {@code FIND_VALUE} operation
     * @param key        the key associated with the value to be located in the network
     * @param miner      the miner whose cryptographic keys are used to sign and validate data storage operations
     * @return           the {@link Block} if found; {@code null} if the value is not present in the network
     */
    public static Block findValue(Node senderNode, String key,Miner miner) {
        String keyId = CryptoUtils.generateKeyId(key);
        Set<Node> queriedNodes = new HashSet<>();
        Set<Node> discoveredNodes = new HashSet<>();
        int i = 1;

        String senderIp = senderNode.getIpAddr();
        int senderPort = senderNode.getPort();

        while (true) {
            if (
                    PeerComunication.sendMessageToPeer(senderIp, senderPort, "GET_ROUTING_TABLE", null)
                            instanceof SecureMessage routingSecureMessage
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

                        if (
                                response instanceof SecureMessage secureMessage
                                && secureMessage.verifySignature()
                                && checkNodeId(secureMessage,node)
                        ) {
                            if(secureMessage.getPayload() instanceof Block foundValue
                               && foundValue != null) {
                                System.out.printf("Value found in [%s,%s,%s]\n", node.getNodeId(), node.getIpAddr(), node.getPort());
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
     * <p>The value is encapsulated in a {@link BlockKeyWrapper}, and a {@link SecureMessage} is created
     * using the {@code miner}'s public and private keys. The message is then securely sent to each of
     * the closest nodes for storage.</p>
     *
     * @param senderNode The node initiating the STORE operation.
     * @param key        The string key to be used for identifying the value (in this case the blockhash).
     * @param value      The value (a Block) to be stored in the network.
     *  @param miner     The {@link Miner} whose keys are used to sign the storage messages securely
     *
     * @note This implementation follows a broadcast-style STORE. We assume that the {@code FIND_NODE}
     *       performed from the senderNode returns all nodes in the network (i.e., the {@code kClosestNodes}
     *       actually correspond to all active Kademlia nodes). This guarantees that the value is propagated
     *       across the entire network.
     */
    public static void store (Node senderNode, String key, Block value, Miner miner){
        String keyId =  CryptoUtils.generateKeyId(key);
        System.out.println("Key Id = " + keyId);
        String senderIp = senderNode.getIpAddr();
        int senderPort = senderNode.getPort();

        // Get the k closest Nodes  to keyId (from sender Node)
        if (
                PeerComunication.sendMessageToPeer(senderIp, senderPort, "FIND_NODE",keyId )
                        instanceof  SecureMessage findSecureMessage
                && findSecureMessage.verifySignature()
                && checkNodeId(findSecureMessage,senderNode)
        ){
            List<Node> kClosestNodes = (List<Node>) findSecureMessage.getPayload();

            // Wraps the Key and Block in a  wrapper class (a.k.a. BlockKeyWrapper)
            BlockKeyWrapper blockKeyWrapper = new BlockKeyWrapper(keyId,value);

            // Store the <Key,Value> pair in those KClosestNodes
            for (Node n : kClosestNodes){
                SecureMessage storeSecureMessage =
                        new SecureMessage("STORE", blockKeyWrapper, miner.getPublicKey(), miner.getPrivateKey());
                PeerComunication.sendMessageToPeer(n.getIpAddr(),n.getPort(),"STORE",storeSecureMessage );
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

    /**
     * Updates the blockchain of a certain Peer
     *
     * @param targetNode  Node/Peer that we are going to send the blockchain
     * @param blockchain  blockchain that will be used to update Peer
     *
     * @note we only use this method when a node first joins a network
     */
    private static void updatePeerBlockchain (Node targetNode, Blockchain blockchain){
        for(Block b : blockchain.getBlockchain()){
            System.out.println(
                    PeerComunication.sendMessageToPeer(
                            targetNode.getIpAddr(), targetNode.getPort(), "ADD_MINED_BLOCK", b
                    )
            );
        }
    }

    /**
     * Verifies the authenticity of a {@link Node}'s claimed ID by comparing it to the computed ID derived
     * from the public key included in the received {@link SecureMessage}.
     *
     * <p>This method is used as a security check to ensure that the {@code Node} claiming to have a certain
     * {@code nodeId} is actually the legitimate owner of that identity, based on its public key.</p>
     *
     * <p>The node ID is recomputed using {@link CryptoUtils#generateSecureNodeId(PublicKey)} from the
     * sender's public key included in the {@code SecureMessage}, and compared against the ID reported
     * by the node {@code n}.</p>
     *
     * @param secureMessage the message containing the sender's public key
     * @param n             the node whose claimed ID will be validated
     * @return              {@code true} if the node's claimed ID matches the computed ID from the public key,
     *                      {@code false} otherwise
     */
    public static boolean checkNodeId(SecureMessage secureMessage, Node n) {
        PublicKey pubKey = secureMessage.getSenderPublickKey();
        String claimedNodeId = n.getNodeId();
        String computedNodeId = CryptoUtils.generateSecureNodeId(pubKey);
        System.out.println("claimedNodeId = " + claimedNodeId );
        System.out.println("computedNodeId = " + computedNodeId);
        System.out.println("Node id is valid? : " + claimedNodeId.equals(computedNodeId));
        return claimedNodeId.equals(computedNodeId);
    }
}
