package Kademlia;

import BlockChain.Block;
import Cryptography.CryptoUtils;
import P2P.KeysUtils;

import java.io.Serializable;
import java.nio.file.Path;
import java.security.PublicKey;
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
    private PublicKey publicKey;

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
        //this.nodeId = generateNodeId(ipAddr,port);
        try {
            this.publicKey = KeysUtils.loadPublicKey(Path.of(KeysUtils.KEYS_DIR + ipAddr + "_" + port + "_PKK"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (this.publicKey == null) printWarning();

        this.nodeId = CryptoUtils.generateSecureNodeId(publicKey);

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
     * Adds node to Routing table
     * @param n
     */
    public  void addToRoutingTable(Node n){
        routingTable.addNodeToBucketList(n);
    }

    /**
     * Adds a block to local Storage
     * @param key
     * @param b
     */
    public void addToLocalStorage(String key, Block b){localStorage.put(key,b);}

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

    private void printWarning(){
        System.out.println("=================================\n" +
                "PUBLICK KEY OF NODE: " + ipAddr + " " + port + " IS NULL\n" +
                        "=================================\n");
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
                "Node Id = "     + nodeId    + ", " +
                "Ip address = "  + ipAddr    + ", " +
                "Port = "        + port      + ", " +
                "Has Publick key = " + (publicKey != null) + "]";
    }
}
