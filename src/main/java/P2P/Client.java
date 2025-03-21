package P2P;

import BlockChain.Transaction;

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

    // TODO: cliente vai ter que ter acesso: Peer


    private static String getOptionsMenu(){
        return "----------------------------------" + '\n' +
                " 0 - Print KBucket" + '\n' +
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
        Scanner scanner = new Scanner(System.in);
        String input = "";
        boolean end = false;
        System.out.println("Please state your a username (has to be unique, but we don't check ;) )");
        String username = scanner.nextLine();
        System.out.println("Welcome " + username + " !!!");

        System.out.println("Please state the hostname of Peer's server");
        String peerServerHost = scanner.nextLine();
        System.out.println("Please state the Port of Peer's server ");
        int peerServertPort = scanner.nextInt();

        System.out.println(getOptionsMenu());
        while (!end){
            System.out.printf("$ ");
            input = scanner.nextLine();
            switch (input){
                case "menu":
                    System.out.println(getOptionsMenu());
                    break;
                case "0": // Routing table info
                    System.out.println("TODO: Print KBucket");
                    break;
                case "1": // FIND_NODE
                    System.out.println("TODO: FIND_NODE");
                    break;
                case "2": // FIND_VALUE
                    System.out.println("TODO: FIND_VALUE");
                    break;
                case "3": // PING
                    System.out.println("TODO: PING");
                    break;
                case "4": // STORE
                    System.out.println("TODO: STORE");
                    break;
                case "5": // Mine a block
                    clientMineHandler(peerServerHost, peerServertPort);
                    break;
                case "6": // Create auction
                    createAuctionHandler(scanner, username, peerServerHost, peerServertPort);
                    break;
                case "7": // Place Bid
                    System.out.println("TODO: Place Bid");
                    break;
                case "8": // Search for available auctions (that are still active)
                    System.out.println("TODO: Search for available auctions");
                    break;
                case "9": // Get Server info
                    System.out.println(sendMessageToPeer(peerServerHost,peerServertPort,"GET_SERVER_INFO",null));
                    break;
                case "10": // Get Server Transaction pool
                    System.out.println(sendMessageToPeer(peerServerHost, peerServertPort,"GET_TRANSACTION_POOL",null));
                    break;
                case "11": // Get Server blockchain
                    System.out.println(sendMessageToPeer(peerServerHost,peerServertPort,"GET_BLOCKCHAIN",null));
                    break;
                case "12": // Get Server Kademlia Node
                    System.out.println(sendMessageToPeer(peerServerHost,peerServertPort,"GET_KADEMLIA_NODE",null));
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


    private static void clientMineHandler(String peerServerHost, int peerServertPort) {
        System.out.println("Waiting for Response from Peer Server...");
        System.out.println("Peer Server Response: " + sendMessageToPeer(peerServerHost, peerServertPort,"MINE",null));
    }
    private static void createAuctionHandler(Scanner scanner, String username, String peerServerHost, int peerServertPort) {
        System.out.println("Insert a name for the auction");
        String auctionId= scanner.nextLine();
        Transaction createAuction =
                new Transaction(username, Transaction.TransactionType.CREATE_AUCTION,
                        auctionId,0,new Date().getTime());
        System.out.println("Waiting for Response from Peer Server...");
        System.out.println(sendMessageToPeer(peerServerHost, peerServertPort,"ADD_TRANSACTION",createAuction));

    }




    /**
     * Sends a string message to a Peer's Server
     * @param serverHost
     * @param serverPort
     * @param messageType can be: FIND_NODE , FIND_VALUE , PING , STORE , TODO talvez mais
     * @param object  if it is null we are not sending anything to Peer's Server.Otherwise we
     *                send the object.
     * @return an object that contains response from Peer Server (either 'OK' or 'Error' message)
     */
    public static Object sendMessageToPeer (String serverHost,int serverPort,Object messageType, Object object){
        Object peerResponse = null;
        try {
            Socket socket = new Socket(InetAddress.getByName(serverHost), serverPort);

            /*
             * Prepare socket I/O channels
             * IMPORTANTE: Criar ObjectOutputStream ANTES de ObjectInputStream
             */
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            /*
             * Send mesage type object to server
             */
            out.writeObject(messageType);
            System.out.printf("SENT %s to server\n",messageType);
            out.flush();

            /*
             * Server response
             */
            peerResponse =  in.readObject();

            /*
             * Send object to server (in case object is not null)
             */
            if(object == null){
                System.out.println("not sending object to server");
            }
            else {
                System.out.println(peerResponse);
                System.out.println("sending object to server");
                out.writeObject(object);
                out.flush();

                /*
                 * Server response (Ok or error message)
                 */
                peerResponse = in.readObject();
            }

            socket.close();



        }catch (Exception e){
            System.out.println("Server @ " +serverPort + " failed to connect." );
        }
        return peerResponse;
    }






}
