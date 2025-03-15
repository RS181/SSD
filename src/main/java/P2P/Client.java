package P2P;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
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
                " exit - Exit" + '\n' +
                "----------------------------------";
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String input = "";
        boolean end = false;
        System.out.println(getOptionsMenu());
        while (!end){
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
                    System.out.println("TODO: Mine a block");
                    break;
                case "6": // Create auction
                    System.out.println("TODO: Create auction");
                    break;
                case "7": // Place Bid
                    System.out.println("TODO: Place Bid");
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

    /**
     * Sends a message to a Peer's Server
     * @param serverHost
     * @param serverPort
     * @param message
     * @return
     */
    public static String sendMessageToPeer (String serverHost,int serverPort,String message){
        String peerResponse = "";
        try {
            Socket socket = new Socket(InetAddress.getByName(serverHost), serverPort);

            /*
             * Prepare socket I/O channels
             */

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /*
             * Send message to server
             */
            out.println(message);
            out.flush();

            /*
             * Server response
             */
            peerResponse = in.readLine();
            socket.close();
        }catch (Exception e){
            System.out.println("Server @ " +serverPort + " failed to connect." );
        }
        return peerResponse;
    }

}
