package P2P;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

/**
 * Class that represents a Peer in our project:
 *  TODO: Peer vai ter que ter acesso: Servidor, Blockchain, Miner e nó kademlia
 */
public class Peer {
    String host;
    int port;
    Logger logger;


    public Peer(String hostname,int port) {
        host = hostname;
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
        new Thread(new Server(args[0], Integer.parseInt(args[1]), peer.logger)).start();
    }
}

class Server implements Runnable {
    String host;
    int port;
    ServerSocket server;
    Logger logger;

    public Server(String host, int port, Logger logger) throws Exception {
        this.host = host;
        this.port = port;
        this.logger = logger;
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
                    logger.info("server: new connection from " + clientAddress);

                    HandleRequest(client);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles Request that came to this server's Peer
     * @param client
     */
    private void HandleRequest(Socket client) {

        try {
            /*
             * Prepare socket I/O channels
             */
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            /*
             * Read the message that was sent by client Socket
             */

            String message = in.readLine();
            System.out.println("Peer Server received: " + message);

            switch (message){
                case "FIND_NODE":
                    findNodeHandler(client,in,out);
                    break;
                case "FIND_VALUE":
                    findValueHandler(client,in,out);
                    break;
                case "PING":
                    pingHandler(client,in,out);
                    break;
                case "STORE":
                    storeHandler(client,in,out);
                    break;
                default:
                    logger.warning("Received unknown message type: " + message);
                    break;
            }

            /*
             * send the response to client
             */
            out.println("< Fim de comunicação entre Cliente/Peer >");
            out.flush();

        } catch (Exception e){
            logger.severe("Peer Server could not handle Request from @" + client.getPort());
        }
    }

    private void findNodeHandler(Socket client, BufferedReader clientIn, PrintWriter clientOut) {
        // TODO
        logger.warning("TODO: implementar FIND_NODE");
    }

    private void findValueHandler(Socket client, BufferedReader clientIn, PrintWriter clientOut) {
       // TODO
        logger.warning("TODO: implementar FIND_VALUE");
    }

    private void pingHandler(Socket client, BufferedReader in, PrintWriter out) {
        // TODO
        logger.warning("TODO: implementar PING");
    }
    private void storeHandler(Socket client, BufferedReader in, PrintWriter out) {
        // TODO
        logger.warning("TODO: implementar STORE");
    }

}

