package P2P;

import BlockChain.Block;
import BlockChain.Transaction;
import Kademlia.Node;
import Kademlia.Operations;

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
                " 0 - Find Kademlia Node " + '\n' +
                " 1 - Find Value" + '\n' +
                " 2 - Ping" + '\n' +
                " 3 - (Test) Store" + '\n' +
                " 4 - Mine Block" + '\n' +
                " 5 - Start Auction " + '\n' +
                " 6 - Stop Auction" + '\n' +
                " 7 - Place Bid" + '\n' +
                " 8 - Search for available Auctions" + '\n' +
                " 9 - Get Server info" + '\n' +
                "10 - Get Server Transaction pool" + '\n' +
                "11 - Get Server blockchain" + '\n' +
                "12 - Get Server Kademlia Node " + '\n' +
                "13 - Get Server Storage " + '\n' +
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
                case "0": // FIND_NODE
                    findNodeHandler(scanner);
                    break;
                case "1": // FIND_VALUE
                    findValueHandler(scanner);
                    break;
                case "2": // PING
                    pingHandler(scanner);
                    break;
                case "3": // STORE
                    clientStoreHandler(scanner);
                    break;
                case "4": // Mine a block
                    clientMineHandler(peerServerHost, peerServerPort);
                    break;
                case "5": // Start auction
                    startAuctionHanlder(scanner);
                    break;
                case "6": // Stop auction
                    stopAuctionHandler(scanner);
                    break;
                case "7": // Place Bid
                    System.out.println("TODO: Place Bid");
                    break;
                case "8": // Search for available auctions (that are still active)
                    System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"GET_AVAILABLE_AUCTIONS",null));
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
                case "13": // Get <Key,Value> from kademlia Node
                    System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"GET_STORAGE",null));
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
        //TODO: ajustar isto para deixar mais claro se estamos a fazer
        // um FIND_NODE ou JOIN_NETWORK
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

    private void findValueHandler(Scanner scanner) {
        System.out.println("Insert the key you want to do FIND_VALUE: ");
        String key = scanner.nextLine();
        Node sender = new Node(peerServerHost,peerServerPort,false);

        Block value = Operations.findValue(sender,key);
        System.out.printf("Result of FIND_VALUE(%s) = %s\n",key,value);
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
        else {
            // TODO enviar o REMOVE PEER para todos os nós conhecidos por PEER
            System.out.println(ipAddr + " " + port + " is OFF-LINE");
        }
    }

    // NOTA: este método é só para realizar testes sobre operação STORE do kademlia
    private void clientStoreHandler(Scanner scanner) {
        System.out.println("Insert the key value: ");
        String key = scanner.nextLine();

        ArrayList<Transaction> startAuction = new ArrayList<>();
        startAuction.add(new Transaction("user1", Transaction.TransactionType.START_AUCTION, "AUC123", 0, System.currentTimeMillis()));
        // No need to mine, because we are just using this for test purposes
        Block value = new Block(startAuction,"");
        Node sender = new Node(peerServerHost,peerServerPort,false);

        Operations.store(sender,key,value);
    }



    private static void clientMineHandler(String peerServerHost, int peerServerPort) {
        System.out.println("Waiting for Response from Peer Server...");
        System.out.println("Peer Server Response: " + PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"MINE",null));
    }

    /*
    private static void createAuctionHandler(Scanner scanner, String username, String peerServerHost, int peerServerPort) {
        System.out.println("Insert a name for the auction you want to Start");
        String auctionName= scanner.nextLine();
        Transaction createAuction =
                new Transaction(username, Transaction.TransactionType.CREATE_AUCTION,
                        auctionName,0,new Date().getTime());
        System.out.println("Waiting for Response from Peer Server...");
        System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"ADD_TRANSACTION",createAuction));
    }
     */
    private void startAuctionHanlder(Scanner scanner) {
        System.out.println("Insert the name for the auction you want to Create and Start");
        String auctionName = scanner.nextLine();
        Transaction startAuction =
                new Transaction(username, Transaction.TransactionType.START_AUCTION,
                        auctionName,0,new Date().getTime());

        System.out.println("Waiting for Response from Peer Server...");
        System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"ADD_TRANSACTION",startAuction));
    }

    private void stopAuctionHandler(Scanner scanner) {
        System.out.println("Insert the name for the auction you want to Stop");
        String auctionName = scanner.nextLine();
        Transaction stopAuction =
                new Transaction(username, Transaction.TransactionType.CLOSE_AUCTION,
                        auctionName,0,new Date().getTime());

        System.out.println("Waiting for Response from Peer Server...");
        System.out.println(PeerComunication.sendMessageToPeer(peerServerHost, peerServerPort,"ADD_TRANSACTION",stopAuction));

    }
}
