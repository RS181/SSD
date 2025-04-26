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
     * Default constructor (only for testing purposes)
     */
    public Node(){
    }

    /**
     * Constructs a new Kademlia node.
     * Initializes the node with its IP address, port, and optionally creates a routing table.
     * It attempts to load the node's public key from a file based on its IP address and port.
     * If the public key cannot be loaded, a warning message is printed, and a new secure node ID
     * is generated based on the (possibly null) public key.
     *
     * @param ipAddr             the IP address of the node.
     * @param port               the port number the node will listen on.
     * @param createRoutingTable {@code true} if a new routing table should be created for this node,
     *                           {@code false} otherwise.
     * @throws RuntimeException  if an error occurs while attempting to load the public key.
     */
    public Node(String ipAddr, int port, boolean createRoutingTable){
        this.ipAddr = ipAddr;
        this.port = port;
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

    /* Getter's & setters (the setters are only here for test purposes) */
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

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /* Auxiliar methods */

    /**
     * Adds node to Routing table
     *
     * @param n node we are trying to add this nodes routing table
     */
    public  void addToRoutingTable(Node n){
        routingTable.addNodeToBucketList(n);
    }

    /**
     * Adds a <key,block> pair to local Storage map
     *
     * @param key key part
     * @param b   block part
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
     *
     * @param key
     * @return a Block if there is a matching in nodes local storage, null otherwise
     */
    public Block getValue(String key){
        return localStorage.get(key);
    }

    /**
     * Just to print a warning (for debugging purposes)
     */
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

    /**
     * Checks if this node id was generated with publick key
     * @return {@code true} if the node's claimed ID matches the computed ID from the public key,
     *         {@code false} otherwise
     */
    public boolean checkNodeId() throws Exception {
        String prefix = ipAddr + "_" + port;
        PublicKey storedPK = KeysUtils.loadPublicKey( Path.of( KeysUtils.KEYS_DIR + prefix + "_Pkk" ) );
        String computedNodeId = CryptoUtils.generateSecureNodeId( storedPK );
        System.out.println("NODE ID CHECK = " + nodeId.equals( computedNodeId ));
        return nodeId.equals( computedNodeId );
    }

}
