package P2P;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/*
 * class that has static methods that relate to Peer comunication
 */
public class PeerComunication {
    /**
     * Sends a string with a certain message type, and possiblily an object, to a Peer's Server
     *
     * @param serverHost  Hostname of Peer server we are going to send message
     * @param serverPort  Port of Peer server we are going to send message
     * @param messageType can have multiple values
     * @param object      if it is null we are not sending anything to Peer's Server.Otherwise, we
     *                    send the object.
     * @return            an object that contains response from Peer Server (either 'OK' or 'Error' message)
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
