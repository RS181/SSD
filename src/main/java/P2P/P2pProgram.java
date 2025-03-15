package P2P;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class that will test diferent aspects of a P2P network
 */
public class P2pProgram {

    public static String sendMessageToPeer (String serverHost,int serverPort,String message){
        String peerResponse = "";
        try {
            Socket socket = new Socket(InetAddress.getByName(serverHost), serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(message);
            out.flush();
            //out.println("TESTE 1234");
            peerResponse = in.readLine();
            socket.close();
        }catch (Exception e){
            System.out.println("Server @ " +serverPort + " failed to connect." );
        }

        return peerResponse;
    }

    public static void main(String[] args) {
       String firstResponse =  sendMessageToPeer("localhost",1234,"FIND_NODE");
       System.out.println("Peer response = " + firstResponse);


    }
}
