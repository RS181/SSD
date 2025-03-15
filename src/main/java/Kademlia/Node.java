package Kademlia;

import Cryptography.CryptoUtils;

/**
 * Class that represents a kademlia Node in a  p2p network
 */
public class Node {
    private String nodeId; // Unique Id
    private String ipAddr;
    private int port;
    private RoutingTable routingTable;


    /**
     * Constructor for a Kademlia Node
     * @param ipAddr
     * @param port
     */
    public Node(String ipAddr, int port){
        this.ipAddr = ipAddr;
        this.port = port;
        this.nodeId = generateNodeId(ipAddr,port);
        this.routingTable = new RoutingTable(nodeId);
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

    /**
     * Generates a Node ID based on the IP address and port.
     * The Node ID is defined as the first 8 bits of the SHA-1 hash of the IP address and port,
     * returned as an 8-character binary string (e.g., "00110001").
     *
     * The first byte of the hash is extracted and converted into an 8-bit binary string.
     *
     * TODO: Confirmar que uma string binario de tamanho 8 é suficiente para identificar o nó
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


    @Override
    public String toString() {
        return "[Node Id = " + nodeId + ", Ip address = "  + ipAddr + ", Port = " + port + "]";
    }
}
