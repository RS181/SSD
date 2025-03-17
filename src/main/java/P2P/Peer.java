package P2P;

import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Kademlia.Node;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
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



    public Peer(String hostname,int port) {
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
        if(args.length < 4){
            System.out.println("Error: Wrong number of arguments. Usage: java Peer <host> <port> <BootStrap_host> <BootStrap_Port>");
            return;
        }
        Peer peer = new Peer(args[0],Integer.parseInt(args[1]));

        System.out.printf("new peer @ host=%s port=%s\n", args[0],args[1]);
        System.out.printf("(TODO) bootstrap peer @ host=%s port=%s\n",args[2],args[3]);
        new Thread(new Server(args[0], Integer.parseInt(args[1]), peer.logger,peer)).start();
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

    ArrayList<Transaction> transactionsPool = new ArrayList<>();

    public Server(String host, int port, Logger logger,Peer peer) throws Exception {
        this.host = host;
        this.port = port;
        this.logger = logger;
        this.peer = peer;
        this.miner = new Miner();
        this.blockchain = new Blockchain();
        this.kademliaNode = new Node(host,port);
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
                    new Thread(new HandleRequest(client,this,logger));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

