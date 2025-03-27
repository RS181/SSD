package Kademlia;

import BlockChain.Block;
import P2P.PeerComunication;

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

    public static void findNodeForKey(Node senderNode, String key, List<Node> keyNearNodes){
        //TODO
    }

    public static void findValue(Node senderNode, String key){
        //TODO
    }

    public static void store (Node senderNode, String key, Block value){

    }
}
