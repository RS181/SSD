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

