package P2P;

import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Kademlia.Node;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

/**
 * Class that represents a Peer in our project.
 * A peer contains:
 * --> Server thread that handles client request
 * --> Blockchain
 * --> Miner
 * --> Kademlia Node
 */
public class Peer {
    String host;
    int port;
    Logger logger;


    public Peer(String hostname, int port) {
        this.host = hostname;
        this.port = port;

        logger = Logger.getLogger("logfile");
        try {
            FileHandler handler = new FileHandler("./src/main/java/P2P/Logs/" + hostname + "_" + port + "_peer.log", false);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //if (args.length < 4) {
        //    System.out.println("Error: Wrong number of arguments. Usage: java Peer <host> <port> " + "<BootStrap_host_1> <BootStrap_Port_1> ... <BootStrap_host_n> <BootStrap_Port_n>");
        //    return;
        //}
        Peer peer = new Peer(args[0], Integer.parseInt(args[1]));

        System.out.printf("new peer @ host=%s port=%s\n", args[0], args[1]);

        ArrayList<Node> bootstrapNodes = new ArrayList<>();
        for (int i = 2; i < args.length; i += 2) {
            Node bootstrap = new Node(args[i], Integer.parseInt(args[i + 1]), false);
            bootstrapNodes.add(bootstrap);
            //System.out.println(bootstrap);
        }
        Server server =new Server(args[0], Integer.parseInt(args[1]), peer.logger, peer, bootstrapNodes);
        new Thread(server).start();

        // Added a shutdown hook to capture when the Peer is turned off
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Peer is shutting down...");
            server.gracefullShutdown();
        }));
    }
}

/**
 * Server class that handles client requests
 */
class Server implements Runnable {
    String host;
    int port;
    ServerSocket server;
    Logger logger;
    Peer peer;
    Miner miner;
    Blockchain blockchain;
    Node kademliaNode;
    Set<Node> knowNeighbours = new HashSet<>(); // Set of neighbours this peer knows
    ArrayList<Transaction> transactionsPool = new ArrayList<>();
    private CopyOnWriteArrayList<ClientHandler> activeClients = new CopyOnWriteArrayList<>(); //thread-safe implementation of a list


    /**
     * Initializes a new instance of the {@code Server} class.
     * <p>
     * This constructor sets up the server with the specified host, port, and logging system.
     * It also initializes a {@code Peer}, a {@code Miner}, a {@code Blockchain}, and a
     * {@code kademliaNode}. Additionally, it sets up the list of known neighbors and updates the
     * Kademlia routing table using the provided bootstrap nodes.
     * </p>
     *
     * @param host           the IP address of the server
     * @param port           the port on which the server will listen
     * @param logger         the logger instance for logging messages
     * @param peer           the peer instance associated with this server
     * @param bootstrapNodes a list of bootstrap nodes to initialize the network
     * @throws Exception if an error occurs while setting up the server socket
     */
    public Server(String host, int port, Logger logger, Peer peer, ArrayList<Node> bootstrapNodes) throws Exception {
        this.host = host;
        this.port = port;
        this.logger = logger;
        this.peer = peer;
        this.miner = new Miner();
        this.blockchain = new Blockchain();
        this.kademliaNode = new Node(host, port, true);

        // Initialize knowNeighbours and Routing table of kademlia Node
        for (Node neihbour : bootstrapNodes) {
            knowNeighbours.add(neihbour);
            kademliaNode.addToRoutingTable(neihbour);
        }
        server = new ServerSocket(port, 1, InetAddress.getByName(host));
    }

    @Override
    public void run() {
        try {
            logger.info("server: endpoint running at port " + port + " ...");
            while (true) {
                try {
                    Socket client = server.accept();
                    String clientAddress = client.getInetAddress().getHostAddress();
                    logger.info("server: new connection from @" + clientAddress + " " + client.getPort());

                    // Creates a thread to handle client request.
                    // This allows the server to handle multiple clients simultaneously
                    ClientHandler requestHandler = new ClientHandler(client, this, logger);
                    activeClients.add(requestHandler);
                    new Thread(requestHandler).start();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add's a node (if it's not already there) to:
     * --> list of kneigbours
     * --> routing table
     * @param n node we want to add to list of kneigbours and routing table
     * @return True if we added the node to list of kneigbours  and routing table.False otherwise
     */
    public boolean addNeighbour(Node n){
        boolean a = true;

        Iterator<Node> iterator = knowNeighbours.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(n)) {
                a = false;
                break;
            }
        }

        boolean b = kademliaNode.getRoutingTable().addNodeToBucketList(n);

        if (a && b)
            knowNeighbours.add(n);

        return  a && b;
    }

    /**
     * Remove a node from:
     * --> list of kneigbours
     * --> routing table
     * @param n node we want to remove from list of kneigbours and routing table
     * @return True if we removed the node from list of kneigbours  and routing table.False otherwise
     */
    public boolean removeNeighbour(Node n){
        boolean a = false;

        Iterator<Node> iterator = knowNeighbours.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(n)) {
                iterator.remove(); // Remove in a thread safe way
                a = true;
            }
        }

        boolean b = kademliaNode.getRoutingTable().removeNodeFromBucketList(n);
        //if (a && b) {
        //    return true;
        // }
        //return false;
        return a && b;
    }

    /**
     * Basicaly when a Peer shutdowns, it sends to all og its
     * kneighbours a message "REMOVE_PEER" to tell each one
     * to remove this Node/Peer from their Neighbours List
     * and routing table
     */
    public void gracefullShutdown(){
        for (Node neighour: knowNeighbours){
            String response = (String) PeerComunication.sendMessageToPeer(
                    neighour.getIpAddr(),neighour.getPort(),
                    "REMOVE_PEER",kademliaNode);
            logger.info(response);
        }
    }

    /**
     * Stop all threads by closing  ClientHandler client socket and
     * reset's list of active clients
     */
    public void stopAllThreads() {
        for (ClientHandler h : activeClients) {
            try {
                h.client.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // Reset activeClients
        activeClients = new CopyOnWriteArrayList<>();
    }
}

