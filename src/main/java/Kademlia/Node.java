package Kademlia;

import BlockChain.Block;
import Cryptography.CryptoUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class that represents a kademlia Node in a  p2p network
 */
public class Node implements Serializable {
    private String nodeId; // Unique Id
    private String ipAddr;
    private int port;
    private RoutingTable routingTable;

    private Map<String, Block> localStorage;

    /**
     * Constructor for a Kademlia Node
     * @param ipAddr
     * @param port
     * @param createRoutingTable indicates whether to create or not the routing table
     *                           for this Kademlia Node
     */
    public Node(String ipAddr, int port, boolean createRoutingTable){
        this.ipAddr = ipAddr;
        this.port = port;
        this.nodeId = generateNodeId(ipAddr,port);
        if (createRoutingTable)
            this.routingTable = new RoutingTable(nodeId,ipAddr,port);
        localStorage = new HashMap<>();
    }

    /* Getter's */
    public String getNodeId() {
        return nodeId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public int getPort() {
        return port;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public Map<String, Block> getLocalStorage() {
        return localStorage;
    }

    /**
     * Generates a Node ID based on the IP address and port.
     * The Node ID is defined as the first 8 bits of the SHA-1 hash of the IP address and port,
     * returned as an 8-character binary string (e.g., "00110001").
     *
     * The first byte of the hash is extracted and converted into an 8-bit binary string.
     *
     * TODO: possivelmente vamos ter que ajustar para mais bits
     *
     * @param ipAddr The IP address of the node.
     * @param port   The port number of the node.
     * @return The generated Node ID as an 8-bit binary string.
     */
    public String generateNodeId(String ipAddr, int port) {
        String input = CryptoUtils.getHash1(ipAddr + port);
        byte[] bytes = input.getBytes();
        int first8Bits = bytes[0] & 0xFF; // Ensures a positive 8-bit value
        return String.format("%8s", Integer.toBinaryString(first8Bits)).replace(' ', '0'); // Ensures 8-bit format
    }

    /**
     * Adds node to Routing table
     * @param n
     */
    public  void addToRoutingTable(Node n){
        routingTable.addNodeToBucketList(n);
    }

    /**
     * Stores a key-value pair in the node's local storage if the key does not already exist.
     *
     * @param key   The key, which corresponds to a key id, used to identify the value.
     * @param value The value, which corresponds to a block, associated with the key.
     */
    public void storeKeyValuePair(String key,Block value){
        if (localStorage.get(key) == null)
            localStorage.put(key,value);
    }

    /**
     * Tries to get the block that corresponds to a certain key
     * @param key
     * @return a Block if there is a matching in nodes local storage, null otherwise
     */
    public Block getValue(String key){
        return localStorage.get(key);
    }

    @Override
    public boolean equals(Object object) {
        Node anotherNode= (Node) object;
        if (
                this.nodeId.equals(anotherNode.nodeId)
                && this.ipAddr.equals(anotherNode.ipAddr)
                && this.port == anotherNode.getPort()
        ) {
            return true;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return Objects.hash(nodeId, ipAddr, port);
    }
    @Override
    public String toString() {
        return "[" +
                "Node Id = "       + nodeId + ", " +
                "Ip address = "  + ipAddr +   ", " +
                "Port = "        + port   +
                "]" ; //+
                //(routingTable != null ? routingTable.toString() : "");
    }
}
