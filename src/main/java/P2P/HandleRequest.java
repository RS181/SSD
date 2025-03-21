package P2P;

import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Kademlia.Node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Class that handles client and other peer's request's
 */
public class HandleRequest implements Runnable {
    Socket client;
    Server server;
    Logger logger;
    Miner miner;
    Blockchain blockchain;
    Node kademliaNode;
    ArrayList<Transaction> transactionsPool;




    public HandleRequest(Socket client, Server server, Logger logger) {
        this.client = client;
        this.server = server;
        this.miner = server.miner;
        this.blockchain = server.blockchain;
        this.kademliaNode = server.kademliaNode;
        this.transactionsPool = server.transactionsPool;
        this.logger = logger;
    }


    @Override
    public void run() {
        try {
            /*
             * Prepare socket I/O channels
             */
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());

            /*
             * Read the message that was sent by client Socket
             */
            Object receivedObject = in.readObject();
            if (receivedObject instanceof String message) {
                System.out.println("Peer Server received: " + message);

                switch (message) {
                    case "FIND_NODE":
                        findNodeHandler(in, out);
                        break;
                    case "FIND_VALUE":
                        findValueHandler(in, out);
                        break;
                    case "PING":
                        pingHandler(in, out);
                        break;
                    case "STORE":
                        storeHandler(in, out);
                        break;
                    case "MINE":
                        mineHandler(in, out);
                        break;
                    case "ADD_TRANSACTION":
                        addTransactionHandler(in, out);
                        break;
                    case "GET_TRANSACTION_POOL":
                        getTransactionPool(in,out);
                        break;
                    case "GET_BLOCKCHAIN":
                        getBlockchain(in,out);
                        break;
                    case "GET_KADEMLIA_NODE":
                        getKademliaNode(in,out);
                        break;
                    case "GET_SERVER_INFO":
                        getServerInfo(in,out);
                        break;
                    case "STOP":
                        server.stopAllThreads();
                        break;
                    default:
                        logger.warning("Received unknown message type: " + message);
                        break;
                }
            } else {
                logger.warning("Received an unknown object type");
            }

            /*
             * send the response to client
             */
            out.writeObject("< Fim de comunicação entre Cliente/Peer >");
            out.flush();

            /*
             * close client socket
             */

            client.close();
            System.out.println("closed client connection");


        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Peer Server could not handle Request from @" + client.getPort());

        }
    }

    private void findNodeHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // TODO
        logger.warning("TODO: implementar FIND_NODE");
    }

    private void findValueHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // TODO
        logger.warning("TODO: implementar FIND_VALUE");
    }

    private void pingHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // TODO
        logger.warning("TODO: implementar PING");
    }

    private void storeHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // TODO
        logger.warning("TODO: implementar STORE");
    }

    /**
     * Responds with 'OK' to client if mining was sucessful.
     * Otherwise, gives an error message
     *
     * @param clientIn
     * @param clientOut
     */
    private void mineHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        //TODO tenho de ter forma de parar o processo de mining caso
        // recebe um bloco minerado !!!!!!

        // Syncronize on transation pool to avoid race conditions between threads
        synchronized (transactionsPool) {
            try {
                if (transactionsPool.size() < 3) {
                    clientOut.writeObject("Dont have enough trasanctions to mine a block");
                    clientOut.flush();
                } else {
                    String prevhash = "";
                    if (blockchain.getLastBlock() != null)
                        prevhash = blockchain.getLastBlock().getBlockHash();
                    // Mine block and try to add to blockchain
                    Block b = miner.mineBlock(new ArrayList<>(transactionsPool), prevhash);
                    if (!blockchain.addBlock(b, miner)) { // if the block isn't valid send erro message
                        logger.severe("Error ocured while adding block to blockchain (mineHandle)");
                        return;
                    }

                    // Reset transactions pool
                    logger.info("Reseting Transactions pool");
                    transactionsPool.clear();

                    clientOut.writeObject("OK");
                    clientOut.flush();
                    //System.out.println(blockchain);

                    // send Stop message to stop all local threads
                    // TODO mais a frente vou ter que fazer isto para todos os Peer's
                    //Client.sendMessageToPeer(server.host,server.port,"STOP",null);

                }
            } catch (Exception e) {
                logger.severe("Error ocured (mineHandler)");
                e.printStackTrace();
            }
        }
    }

    /**
     * Responds with 'OK' to client if mining was sucessful.
     * Otherwise, gives an error message
     *
     * @param clientIn
     * @param clientOut
     */
    private void addTransactionHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on transactionsPool to avoid race conditions between threads
        synchronized (transactionsPool) {
            try {
                logger.info("Adding transaction ...");
                clientOut.writeObject("OK");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof Transaction t) {

                    transactionsPool.add(t);
                    clientOut.writeObject("OK");

                    System.out.println(t);

                } else {
                    clientOut.writeObject("Error: Only accept transactions");
                    logger.warning("Error: Did not receive a transaction (addTransactionHandler)");
                }
            } catch (Exception e) {
                logger.severe("Error ocured (addTransactionHandler)");
                //throw new RuntimeException(e);
            }
        }

    }
    private void getTransactionPool(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on transactionsPool to avoid race conditions between threads
        synchronized (transactionsPool){
            try{
                clientOut.writeObject(transactionsPool);
                clientOut.flush();
            }catch (Exception e){
                logger.severe("Error ocured (getTransactionPool)");
            }
        }
    }

    private void getBlockchain(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on blockchain to avoid race conditions between threads
        synchronized (blockchain){
            try {
                clientOut.writeObject(blockchain);
                clientOut.flush();
            } catch (Exception e){
                logger.severe("Error ocured (getBlockchain)");
            }
        }
    }
    private void getKademliaNode(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on kademliaNode to avoid race conditions between threads
        synchronized (kademliaNode){
            try {
                clientOut.writeObject(kademliaNode + "\n" + kademliaNode.getRoutingTable());
                clientOut.flush();
            } catch (Exception e){
                logger.severe("Error ocured (getKademliaNode)");
                e.printStackTrace();
            }
        }
    }

    private void getServerInfo(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        try{
            clientOut.writeObject(
                    "Address = " + server.host + '\n' +
                    "Port = "    + server.port + '\n' +
                    "Set of Neighbours = " + server.knowNeighbours + '\n'
            );
            clientOut.flush();
        }catch (Exception e){
            logger.severe("Error ocured (getServerInfo)");
            e.printStackTrace();
        }

    }

}
