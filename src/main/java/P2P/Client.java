package P2P;

import BlockChain.Transaction;
import Kademlia.Node;
import Kademlia.Operations;
import Kademlia.RoutingTable;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Class that serves as an interface to the client
 * so that he can use the system
 */
public class Client {

    private final String username;
    private final String peerServerHost;
    private final int peerServerPort;

    public Client(String username,String peerServerHost, int peerServerPort){
        this.username = username;
        this.peerServerHost = peerServerHost;
        this.peerServerPort = peerServerPort;
    }

    private static String getOptionsMenu(){
        return "----------------------------------" + '\n' +
                " 0 - NONE " + '\n' +
                " 1 - Find Kademlia Node" + '\n' +
                " 2 - Find Value" + '\n' +
                " 3 - Ping" + '\n' +
                " 4 - Store" + '\n' +
                " 5 - Mine Block" + '\n' +
                " 6 - Create Auction" + '\n' +
                " 7 - Place Bid" + '\n' +
                " 8 - Search for available Auctions" + '\n' +
                " 9 - Get Server info" + '\n' +
                "10 - Get Server Transaction pool" + '\n' +
                "11 - Get Server blockchain" + '\n' +
                "12 - Get Server Kademlia Node " + '\n' +
                " exit - Exit" + '\n' +
                "----------------------------------";
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Client <username> <peerServerHost> <peerServerPort>");
            return;
        }

        String username = args[0];
        String peerServerHost = args[1];
        int peerServerPort = Integer.parseInt(args[2]);
        Client client = new Client(username,peerServerHost,peerServerPort);
        client.start();
    }

    private  void start() {
        Scanner scanner = new Scanner(System.in);
        boolean end = false;

        String input;
        System.out.println(getOptionsMenu());
        while (!end){
            System.out.printf("$ ");
            input = scanner.nextLine();
            switch (input){
                case "menu":
                    System.out.println(getOptionsMenu());
                    break;
                case "0":
                    // todo: este ponto esta vazio
                    break;
                case "1": // FIND_NODE
                    findNodeHandler(scanner);
                    break;
                case "2": // FIND_VALUE
                    System.out.println("TODO: FIND_VALUE");
                    break;
                case "3": // PING
                    pingHandler(scanner);
                    break;
                case "4": // STORE
                    System.out.println("TODO: STORE");
                    break;
                case "5": // Mine a block
                    clientMineHandler(peerServerHost, peerServerPort);
                    break;
                case "6": // Create auction
                    createAuctionHandler(scanner, username, peerServerHost, peerServerPort);
                    break;
                case "7": // Place Bid
                    System.out.println("TODO: Place Bid");
                    break;
                case "8": // Search for available auctions (that are still active)
                    System.out.println("TODO: Search for available auctions");
                    break;
                case "9": // Get Server info
                    System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"GET_SERVER_INFO",null));
                    break;
                case "10": // Get Server Transaction pool
                    System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"GET_TRANSACTION_POOL",null));
                    break;
                case "11": // Get Server blockchain
                    System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"GET_BLOCKCHAIN",null));
                    break;
                case "12": // Get Server Kademlia Node
                    System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"GET_KADEMLIA_NODE",null));
                    break;
                case "exit": // Exit
                    end = true;
                    break;
                default:
                    System.out.println("Choose valid input");
                    break;
            }
        }
    }

    private void findNodeHandler(Scanner scanner) {
        System.out.println("Insert the Ip address for FIND_NODE");
        String ipAddr = scanner.nextLine();
        System.out.println("Insert the Port for FIND_NODE");
        int port = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Do you want to do join network? (yes or no)");
        boolean joinNetwork = scanner.nextLine().equals("yes");
        Node sender = new Node(peerServerHost,peerServerPort,joinNetwork);
        Node target = new Node(ipAddr,port,false);

        if (joinNetwork){
            System.out.println(peerServerHost + " " + peerServerPort + " --[JOIN_NETWORK]--> " + ipAddr + " " + port);
            Operations.joinNetwork(sender, target);
        } else {
            System.out.println(peerServerHost + " " + peerServerPort + " --[FIND_NODE]--> " + ipAddr + " " + port);
            Operations.findNode(sender, target.getNodeId());
        }
    }

    private void pingHandler(Scanner scanner) {
        System.out.println("Insert the Ip address of Kademlia Node you want to PING");
        String ipAddr = scanner.nextLine();
        System.out.println("Insert the Port of Kademlia Node you want to PING");
        int port = scanner.nextInt();

        System.out.println(peerServerHost + " " + peerServerPort + " --[PING]--> " + ipAddr + " " + port);
        Node sender = new Node(peerServerHost,peerServerPort,false);
        Node target = new Node(ipAddr,port,false);

        Boolean pingWasSucessful = Operations.ping(sender,target);
        if(pingWasSucessful)
            System.out.println(ipAddr + " " + port + " is ON-LINE");
        else
            System.out.println(ipAddr + " " + port + " is OFF-LINE");
    }


    private static void clientMineHandler(String peerServerHost, int peerServerPort) {
        System.out.println("Waiting for Response from Peer Server...");
        System.out.println("Peer Server Response: " + PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"MINE",null));
    }
    private static void createAuctionHandler(Scanner scanner, String username, String peerServerHost, int peerServerPort) {
        System.out.println("Insert a name for the auction");
        String auctionId= scanner.nextLine();
        Transaction createAuction =
                new Transaction(username, Transaction.TransactionType.CREATE_AUCTION,
                        auctionId,0,new Date().getTime());
        System.out.println("Waiting for Response from Peer Server...");
        System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"ADD_TRANSACTION",createAuction));

    }

}
