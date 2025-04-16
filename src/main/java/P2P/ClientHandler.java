package P2P;

import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Kademlia.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class that handles client and other peer's request's
 */
public class ClientHandler implements Runnable {
    Socket client;
    Server server;
    Logger logger;
    Miner miner;
    Blockchain blockchain;
    Node kademliaNode;
    ArrayList<Transaction> transactionsPool;


    public ClientHandler(Socket client, Server server, Logger logger) {
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
                    // Kademlia related methods
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

                    // Kademlia/App related methods
                    case "REMOVE_PEER":
                        removePeerHandler(in,out);
                        break;
                    case "ADD_PEER":
                        addPeerHandler(in,out);
                        break;
                    case "ADD_TO_STORAGE":
                        addToStorageHandler(in,out);
                        break;

                    // Blockchain/App related methods
                    case "MINE":
                        mineHandler(out);
                        break;
                    case "ADD_MINED_BLOCK": // Sent from Peer to Peer
                        addMinedBlockHandler(in,out);
                        break;
                    case "ADD_TRANSACTION":
                        addTransactionHandler(in, out);
                        break;

                    // App only related methods
                    case "GET_AVAILABLE_AUCTIONS":
                        getAvailableAuctions(out);
                        break;
                    case "GET_PLACED_BIDS":
                        getAvailableBids(in,out);
                        break;
                    case "GET_TRANSACTION_POOL":
                        getTransactionPool(out);
                        break;
                    case "GET_BLOCKCHAIN":
                        getBlockchain(out);
                        break;
                    case "GET_KADEMLIA_NODE":
                        getKademliaNode(out);
                        break;
                    case "GET_STORAGE":
                        getStorage(out);
                        break;
                    case "GET_ROUTING_TABLE":
                        getRoutingTable(out);
                        break;
                    case "GET_MINER":
                        getMiner(out);
                    case "GET_SERVER_INFO":
                        getServerInfo(out);
                        break;
                    case "STOP": // Stop Mining block
                        System.out.println("GOING TO STOP server threads and Miner");
                        server.stopAllThreads();
                        server.miner.stopMining();
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
        } catch (SocketException e) {
            logger.warning("Caught SocketException in ClientHandler (run)");
            //e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Caught IOException or ClassNotFoundException in ClientHandler (run)");
            //e.printStackTrace();
        }
    }

    private void addToStorageHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        logger.info("Received Add to 'kademlia' node's Storage");
        try {

            synchronized (kademliaNode) {
                clientOut.writeObject("OK from @ " + server.host + " " + server.port);
                clientOut.flush();

                Object receivedObject = clientIn.readObject();
                
                if (receivedObject instanceof Block b ){
                    logger.info("Received Block to add to 'kademlia' node's Storage = [" + b.getBlockHash() + "]");
                    String keyId = Operations.generateKeyId(b.getBlockHash());

                    kademliaNode.addToLocalStorage(keyId,b);
                    clientOut.writeObject("Updated Kademlia Node storage \n:" + kademliaNode.getLocalStorage());
                }else {
                    clientOut.writeObject("Error: Expected Block but received something else");
                    logger.warning("Error: Did not receive a Block (addToStorageHandler)");

                }
            }
        }catch (Exception e){
            logger.severe("Error ocured (addToStorageHandler)");

        }
    
    }

    /**
     *
     * @param clientIn
     * @param clientOut
     */
    private void findNodeHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        logger.info("Received Find Node message");
        try {
            // Syncronize on kademlia Node to avoid race conditions between threads
            synchronized (kademliaNode) {
                clientOut.writeObject("OK from @ " + server.host + " " + server.port);
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof String targetNodeId){
                    logger.info("Receive FIND_NODE(" + targetNodeId + ")");
                    List<Node> kClosestNodes =
                            kademliaNode.getRoutingTable().getClosestNodes(Constants.MAX_RETURN_FIND_NODES,targetNodeId);

                    SecureMessage secureMessage = new SecureMessage("FIND_NODE",kClosestNodes
                            ,miner.getPublicKey(),miner.getPrivateKey());

                    //clientOut.writeObject(kClosestNodes);
                    clientOut.writeObject(secureMessage);

                }else {
                    clientOut.writeObject("Error: Expected Node Id but received something else");
                    logger.warning("Error: Did not receive a Node Id (findNodeHandler)");
                }
            }
        } catch (Exception e) {
            logger.severe("Error ocured (findNodeHandler)");
        }
    }

    private void findValueHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        logger.info("Received Find Value message");
        try {
            // Syncronize on kademlia Node to avoid race conditions between threads
            synchronized (kademliaNode){
                clientOut.writeObject("OK");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof String keyId){
                    Block b = kademliaNode.getValue(keyId);
                    if (b != null){
                        logger.info("Value found in local storage");
                        SecureMessage secureMessage =
                                new SecureMessage("FIND_VALUE",b,
                                        miner.getPublicKey(),miner.getPrivateKey());
                        clientOut.writeObject(secureMessage);
                        //clientOut.writeObject(b);
                    }
                    else {
                        logger.warning("Value not found in local storage");
                        List<Node>  closestNodestoKey =
                            kademliaNode.getRoutingTable().getClosestNodes
                                    (Constants.MAX_ROUTING_FIND_NODES,keyId);
                        SecureMessage secureMessage =
                                new SecureMessage("FIND_VALUE",closestNodestoKey,
                                        miner.getPublicKey(),miner.getPrivateKey());
                        clientOut.writeObject(secureMessage);
                        //clientOut.writeObject(closestNodestoKey);
                    }
                }else{
                    clientOut.writeObject("Error: Expected String but received something else");
                    logger.warning("Error: Did not receive a String (findValueHandler)");
                }
            }
        }catch (Exception e){
            logger.severe("Error ocured (findValueHandler)");
        }
    }

    /**
     *
     * @param clientIn
     * @param clientOut
     */
    private void pingHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        try{
            logger.info("Received Ping ...");
            clientOut.writeObject("OK");
            clientOut.flush();

            Object receivedObject = clientIn.readObject();


            if (receivedObject instanceof  SecureMessage secureMessage &&
                    secureMessage.verifySignature() &&
                    secureMessage.getPayload() instanceof Node n
            ){
                logger.info("Received Ping from " + n);
                clientOut.writeObject("OK");
            }else {
                clientOut.writeObject("Error: Expected SecureMessage but received something else");
                logger.warning("Error: Did not receive a SecureMessage or signature is invalid (pingHandler)");
            }
        }catch (Exception e){
            logger.severe("Error ocured (pingHandler)");
        }
    }

    private void storeHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        logger.info("Received Store <Key,Value> message");
        // Syncronize on kademliaNode to avoid race conditions between threads
        synchronized (kademliaNode){
            try {
                clientOut.writeObject("Ok");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof SecureMessage secureMessage
                        && secureMessage.verifySignature()
                        && secureMessage.getPayload() instanceof BlockKeyWrapper blockKeyWrapper){
                    logger.info("Received Key/Block to Store");

                    kademliaNode.storeKeyValuePair(
                            blockKeyWrapper.getKeyId(),blockKeyWrapper.getBlock()
                    );
                    clientOut.writeObject("Store complete");
                }else{
                    clientOut.writeObject("Error: could be caused by bad signature or other comunication error (storeHandler)");
                    logger.warning("Error: Did not receive a BlockKeyWrapper (storeHandler)");
                }

            } catch (Exception e) {
                logger.severe("Error ocured (storeHandler)");
            }
        }
    }

    /**
     *
     * @param clientIn
     * @param clientOut
     */
    private void removePeerHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on server to avoid race conditions between threads
        synchronized (server){
            try{
                logger.info("Removing Peer/Node...");
                clientOut.writeObject("OK");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof Node n){
                    logger.info("Peer/Node we are going to remove " + n);
                    //clientOut.writeObject("OK");
                    if (server.removeNeighbour(n))
                        clientOut.writeObject("OK");
                    else {
                        System.out.println("ERRO OCORREU EM removePeerHandler");
                        clientOut.writeObject("NOT OK");
                    }
                }else{
                    clientOut.writeObject("Error: Expected Node but received something else");
                    logger.warning("Error: Did not receive a Node (removePeerHandler)");
                }
            }catch (Exception e){
                logger.severe("Error ocured (removePeerHandler)");
            }
        }
    }

    /**
     *
     * @param clientIn
     * @param clientOut
     */
    private void addPeerHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on server to avoid race conditions between threads
        synchronized (server){
            try{
                logger.info("Adding Peer/Node...");
                clientOut.writeObject("OK");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof Node n){
                    logger.info("Peer/Node we are going to try to Add "+ n);
                    if (server.addNeighbour(n)) {
                        System.out.println("Added " + n + "(addPeerHandler)");
                        clientOut.writeObject("Ok ( Added " + n + ") to " + server.host + " " + server.port);
                    } else {
                        System.out.println("Could not add " + n + " (addPeerHandler)");
                        clientOut.writeObject("NOT OK ( couldn't add " + n + " ) to " + server.host + " " + server.port);
                    }
                }else{
                    clientOut.writeObject("Error: Expected Node but received something else");
                    logger.warning("Error: Did not receive a Node (addPeerHandler)");
                }
            }catch (Exception e){
                logger.severe("Error ocured (addPeerHandler)");
            }
        }
    }

    /**
     * Handles the mining process and sends the result back to the client.
     *
     * <p>If there are not enough transactions in the pool to mine a block, an error message
     * is sent to the client. Otherwise, a new block is mined using the current transactions,
     * added to the blockchain (if valid), and broadcast to the network.</p>
     *
     * <p>After successful mining:
     * <ul>
     *   <li>The transaction pool is cleared.</li>
     *   <li>A "STOP" message is sent to all known neighbors to halt their mining processes.</li>
     *   <li>An "ADD_MINED_BLOCK" message is sent so peers can update their blockchains.</li>
     *   <li>A "STORE" operation is performed to replicate the block across the network.</li>
     * </ul>
     *
     * <p>This method synchronizes on the {@code transactionsPool} to prevent race conditions
     * in multi-threaded environments.</p>
     *
     * @param clientOut the output stream used to communicate with the client
     */
    private void mineHandler(ObjectOutputStream clientOut) {
        synchronized (transactionsPool) {
            try {
                if (transactionsPool.size() < 1) {
                    clientOut.writeObject("Dont have enough trasanctions to mine a block");
                    clientOut.flush();
                } else {
                    // Reset miner (to garante that stopMining = false in the begining)
                    miner.canStartMining();

                    logger.info("Received MINE message");
                    String prevhash = "";
                    if (blockchain.getLastBlock() != null)
                        prevhash = blockchain.getLastBlock().getBlockHash();

                    logger.info("Started Mining Block ...");
                    Block b = miner.mineBlock(new ArrayList<>(transactionsPool), prevhash);
                    logger.info("Finished Mining Block !!!");

                    // Check if client's socket is still open
                    if (!client.isClosed()) {
                        System.out.println("Client Socket is open");
                        if (!blockchain.addBlock(b, miner.publicKey)) { // if the block isn't valid send erro message
                            logger.severe("Error ocured while adding block to blockchain (mineHandle)");
                            return;
                        }
                        // Reset transactions pool
                        logger.info("Reseting Transactions pool");
                        transactionsPool.clear();

                        clientOut.writeObject("OK");
                        clientOut.flush();


                        for (Node n : server.knowNeighbours) {
                            logger.info("Sending STOP to @" + n.getIpAddr() + " " + n.getPort());
                            // send STOP message to stop all Threads of Neighbours
                            PeerComunication.sendMessageToPeer(n.getIpAddr(), n.getPort(), "STOP", null);
                            // send ADD_MINED_BLOCK message to add block to Neighbours blockchain
                            PeerComunication.sendMessageToPeer(n.getIpAddr(),n.getPort(),"ADD_MINED_BLOCK",b);
                        }

                        // Make a STORE operation (so that we broadcast the block to
                        // sender's k the closest nodes, and each one saves them in local storage)
                        Node sender = new Node(server.host,server.port,false);
                        Operations.store(sender,b.getBlockHash(),b,miner);

                    } else
                        System.out.println("Client Socket is closed");
                }
            } catch (SocketException e) {
                logger.warning("Socket Closed while peer was mining (mineHandler)");
                return;
            } catch (IOException e) {
                logger.warning("Error ocured in I/O (mineHandler)");
            } catch (NullPointerException e){
                e.printStackTrace();
                logger.warning("NullPointerException Error in mineHandler");
            }
        }
    }

    /**
     *
     * @param clientIn
     * @param clientOut
     */
    private void addMinedBlockHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on blockchain to avoid race conditions between threads
        synchronized (blockchain){
                try{
                    logger.info("Adding Mined block sent by peer ...");
                    clientOut.writeObject("OK");
                    clientOut.flush();

                    Object receivedObject = clientIn.readObject();

                    if (receivedObject instanceof Block b){
                        System.out.println("RECEIVED BLOCK FROM PEER");
                        blockchain.addBlock(b,b.getMinerPublicKey());
                        clientOut.writeObject("OK");
                    }else {
                        clientOut.writeObject("Error: Exepted block but receveid somethin else");
                        logger.warning("Error: Did not receive a block (addMinedBlockHandler)");
                    }
                }catch (Exception e){
                    logger.severe("Error ocured (addMinedBlockHandler)");
                }
        }
    }

    /**
     * Handles the addition of a new transaction received from a client over a network stream.
     * <p>
     * This method reads a {@link Transaction} object sent by the client via {@link ObjectInputStream},
     * validates it, and attempts to add it to the local transaction pool, {@code transactionsPool},
     * based on specific rules:
     * <ul>
     *   <li>Only transactions deemed valid by {@code checkTransaction(t, t.getType())} are accepted.</li>
     *   <li>If the transaction type is {@code PLACE_BID}, the pool must be empty (i.e., size &lt; 1) to accept it.</li>
     *   <li>For other transaction types, the pool must have less than 3 transactions (i.e., size &lt; 3).</li>
     * </ul>
     * The method is synchronized on {@code transactionsPool} to prevent race conditions between concurrent threads.
     *
     * @param clientIn the input stream of the client
     * @param clientOut the output stream of the client
     */
    private void addTransactionHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on transactionsPool to avoid race conditions between threads
        synchronized (transactionsPool) {
            try {
                logger.info("Adding transaction ...");
                clientOut.writeObject("OK");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof Transaction t && checkTransaction(t,t.getType())) {
                        if(!t.getType().equals(Transaction.TransactionType.PLACE_BID) &&
                                !t.getType().equals(Transaction.TransactionType.CLOSE_AUCTION)) {
                            if (transactionsPool.size() < 3) {
                                transactionsPool.add(t);
                                clientOut.writeObject("OK");
                            } else {
                                clientOut.writeObject("Not Ok: transanctionsPool has " +
                                        "reached the limit of uncommmited transactions" +
                                        "\n(Please send Mine block --> press 4))");
                            }
                        }else {
                            if(transactionsPool.size() >= 1){
                                clientOut.writeObject("Not Ok: Please commit your local transactions " +
                                        "before placing a Bid or Closing an auction" +
                                        "\n(Please send Mine block --> press 4))");
                            }
                            else {
                                transactionsPool.add(t);
                                clientOut.writeObject("OK");
                            }
                        }
                } else {
                    clientOut.writeObject("NOT OK: Invalid transaction");
                }
            } catch (Exception e) {
                logger.severe("Error ocured (addTransactionHandler)");
                e.printStackTrace();
            }
        }
    }

    private Boolean checkTransaction(Transaction t, Transaction.TransactionType type){
        Boolean ans = true;
        switch (type){
            case START_AUCTION:
                System.out.println("Received START_AUCTION");
                ans = checkStartAuction(t);
                System.out.printf("Check START_AUCTION %s = %s\n",t.getAuctionId(),ans);
                break;
            case CLOSE_AUCTION:
                // TODO: enviar vencedor (i.e. cliente com > transação PLACE_BID desse auction  )
                System.out.println("Received STOP_AUCTION");
                ans = checkStopAuction(t);
                // if close auction check was sucessfull (anounce winner)
                if (ans)
                    anounceAuctionWinner(t.getAuctionId());
                System.out.printf("Check STOP_AUCTION %s = %s\n",t.getAuctionId(),ans);
                break;
            case PLACE_BID:
                System.out.println("Received PLACE_BID");
                ans = checkPlaceBid(t);
                System.out.printf("Check PLACE_BID %s = %s\n",t.getAuctionId(),ans);
                break;
            default:
                System.out.println("Error Unkown Transaction type");
                break;
        }
        return ans;
    }

    /**
     * Checks whether a {@code START_AUCTION} transaction can be accepted based on the current state
     * of the blockchain and the transaction pool.
     * <p>
     * This method ensures that there is no existing auction (either already started or pending in
     * the transaction pool) with the same {@code auctionId} as the one provided. It prevents
     * duplicate {@code START_AUCTION} transactions for the same auction.
     * </p>
     * @param t The {@code START_AUCTION} transaction to validate.
     * @return {@code true} if no auction with the same ID exists in the blockchain or transaction pool;
     *         {@code false} otherwise.
     * @note here we don't accept START_AUCTION transactions
     *       with the same auctionId, that means that a client
     *       can't start more than one auction with the
     *       same auctionName, because auctionId = auctionName+username.
     *       This maintains even if the auction has CLOSED!!!
     */
    private Boolean checkStartAuction(Transaction t) {
        String auctionId = t.getAuctionId();
        Set<String> availableAuctions = blockchain.getAvailableAuctions();
        for (String auction : availableAuctions){
            if(auction.equals(auctionId))
                return false;
        }
        for (Transaction tp : transactionsPool){
            if(tp.getAuctionId().equals(auctionId))
                return false;
        }
        return true;
    }

    /**
     * Checks whether a {@code CLOSE_AUCTION} transaction is valid by verifying that a corresponding
     * {@code START_AUCTION} transaction exists either in the blockchain or in the transaction pool.
     * <p>
     * This ensures that an auction can only be stopped if it has already been started.
     *
     * @param t The {@code CLOSE_AUCTION} transaction to validate.
     * @return {@code true} if a matching {@code START_AUCTION} transaction with the same auction ID
     *         exists in the blockchain or transaction pool; {@code false} otherwise.
     *
     * @note Only the user that made the {@code START_AUCTION} can {@code CLOSE_AUCTION}
     */
    private Boolean checkStopAuction(Transaction t) {
        String auctionId = t.getAuctionId();
        Set<String> availableAuctions = blockchain.getAvailableAuctions();
        for (String auction : availableAuctions){
            if(auction.equals(auctionId))
                return true;
        }
        for (Transaction tp : transactionsPool){
            if(tp.getAuctionId().equals(auctionId) &&
                    tp.getType().equals(Transaction.TransactionType.START_AUCTION))
                return true;
        }
        return false;
    }

    private void anounceAuctionWinner(String auctionId) {
        Set<String> bids = blockchain.getAllBids(auctionId);
        String highestBidder = null;
        double highestBid = Double.NEGATIVE_INFINITY;
        for (String bidEntry : bids) {
            String[] parts = bidEntry.split(":");
            if (parts.length != 2) continue; // ignore invalid entries

            String username = parts[0];
            double bidAmount;

            try {
                bidAmount = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                continue; // ignore invalid entries
            }

            if (bidAmount > highestBid) {
                highestBid = bidAmount;
                highestBidder = username;
            }
        }
        logger.info("Winner of auction [" + auctionId + "] is ===> " + highestBidder + " !!!!!!");
    }

    /**
     * Validates whether a {@code PLACE_BID} transaction is acceptable based on current auction state and bid history.
     * <p>
     * The method performs several checks to determine if the given bid can be added to the blockchain:
     * <ol>
     *   <li>Checks if the bid is being placed on an active/available auction (based on the blockchain's current state).</li>
     *   <li>Gathers all previous bids for the same auction from the blockchain.</li>
     *   <li>Sorts existing bids in descending order by bid amount.</li>
     *   <li>If no previous bids exist, the new bid is considered valid.</li>
     *   <li>If previous bids exist, the new bid must be greater than the current highest bid to be valid.</li>
     * </ol>
     * </p>
     * @param t the {@link Transaction} representing the bid to be validated
     * @return {@code true} if the bid is valid (auction exists and the bid is higher than any existing bid); {@code false} otherwise
     */
    private Boolean checkPlaceBid(Transaction t) {
        String auctionId = t.getAuctionId();
        Set<String> availableAuctions = blockchain.getAvailableAuctions();
        //1. check if Place bid is made to an available auction (if not return false)
        Boolean auctionExists = false;
        for(String auction : availableAuctions){
            if(auction.equals(auctionId)){
                auctionExists = true;
                break;
            }
        }

        if (!auctionExists) return false;

        //2. Get all bids that are made to this auctionId  and are in the Blockchain
        Set<Transaction> existingBids = new HashSet<>();
        for (Block b : blockchain.getBlockchain()){
            for (Transaction tp : b.getTransactions()){
                if(tp.getAuctionId().equals(auctionId) &&
                        tp.getType().equals(Transaction.TransactionType.PLACE_BID)){
                    existingBids.add(tp);
                }
            }
        }

        //3. Sort the bids in descending order of bid amount
        List<Transaction> sortedBids = new ArrayList<>(existingBids);
        sortedBids.sort(Comparator.comparingDouble(Transaction::getBidAmount).reversed());

        //3.1 if there is no bids, the bid received is definitely the bigger one
        if(sortedBids.size() == 0) return true;

        //4. Check if current bid is bigger that first bid in sorted list
        Double biggestExistingBid = sortedBids.get(0).getBidAmount();
        if (t.getBidAmount() > biggestExistingBid)
                return true;

        //System.out.printf("Existing bids  to %s = %s\n",auctionId,sortedBids);
        return false;
    }

    /**
     * Sends the currently available auctions in Peer to the client.
     * <p>
     * This method retrieves the list of available auctions from the blockchain, considering only the
     * transactions that are already confirmed and stored in blocks (i.e., it ignores any transactions
     * still in the transaction pool). An auction is considered available if it has been
     *  {@code START_AUCTION}, but not {@code CLOSE_AUCTION}.
     * <p>
     * The blockchain access is synchronized to ensure thread safety.
     *
     * @param clientOut the output stream of the client
     */
    private void getAvailableAuctions(ObjectOutputStream clientOut) {
        synchronized (blockchain){
            try{
                clientOut.writeObject(blockchain.getAvailableAuctions());
                clientOut.flush();
            }catch (Exception e){
                logger.severe("Error ocured (getAvailableAuctions)");
            }
        }
    }

    private void getAvailableBids(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        synchronized (blockchain){
            try{
                clientOut.writeObject("OK");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();
                if (receivedObject instanceof String auctionId){
                    clientOut.writeObject(blockchain.getAllBids(auctionId));
                }else {
                    clientOut.writeObject("Error: Expected String but received something else");
                    logger.warning("Error: Did not receive a String (getAvailableBids)");
                }
            }catch (Exception e){
                logger.severe("Error ocured (getAvailableBids)");
            }
        }
    }

    /**
     *  Sends the transaction pool stored in the peer to the client.
     * <p>
     * This method synchronizes on the {@code transactionsPool} to prevent race conditions
     * between threads. It then writes the transaction pool to the given output stream.
     * </p>
     *
     * @param clientOut the output stream of the client
     */
    private void getTransactionPool(ObjectOutputStream clientOut) {
        // Syncronize on transactionsPool to avoid race conditions between threads
        synchronized (transactionsPool) {
            try {
                clientOut.writeObject(transactionsPool);
                clientOut.flush();
            } catch (Exception e) {
                logger.severe("Error ocured (getTransactionPool)");
            }
        }
    }

    /**
     *  Sends the blockchain stored in the peer to the client.
     * <p>
     * This method synchronizes on the {@code blockchain} to prevent race conditions
     * between threads. It then writes the blockchain to the given output stream.
     * </p>
     *
     * @param clientOut the output stream of the client
     */
    private void getBlockchain(ObjectOutputStream clientOut) {
        // Syncronize on blockchain to avoid race conditions between threads
        synchronized (blockchain) {
            try {
                clientOut.writeObject(blockchain);
                clientOut.flush();
            } catch (Exception e) {
                logger.severe("Error ocured (getBlockchain)");
            }
        }
    }

    /**
     *  Sends the kademlia Node info stored in the peer to the client.
     * <p>
     * This method synchronizes on the {@code kademliaNode} to prevent race conditions
     * between threads. It then writes the kademlia Node info to the given output stream.
     * </p>
     *
     * @param clientOut the output stream of the client
     */
    private void getKademliaNode(ObjectOutputStream clientOut) {
        // Syncronize on kademliaNode to avoid race conditions between threads
        synchronized (kademliaNode) {
            try {
                clientOut.writeObject(kademliaNode + "\n" + kademliaNode.getRoutingTable());
                clientOut.flush();
            } catch (Exception e) {
                logger.severe("Error ocured (getKademliaNode)");
            }
        }
    }

    /**
     * Sends all <Key,Value> pairs (a.k.a. Storage) to the client
     *
     * @param clientOut
     */
    private void getStorage(ObjectOutputStream clientOut) {
        synchronized (kademliaNode) {
            try {
                SecureMessage secureMessage =
                        new SecureMessage("GET_STORAGE",kademliaNode.getLocalStorage(),
                                miner.getPublicKey(),miner.getPrivateKey());
                //clientOut.writeObject(kademliaNode.getLocalStorage());
                clientOut.writeObject(secureMessage);
                clientOut.flush();
            } catch (Exception e) {
                logger.severe("Error ocured (getStorage)");
            }
        }
    }

    /**
     *  Sends the Peer Server info to the client.
     * @param clientOut the output stream of the client
     */
    private void getServerInfo(ObjectOutputStream clientOut) {
        try {
            clientOut.writeObject(
                            "Address = " + server.host + '\n' +
                            "Port = " + server.port + '\n' +
                            "Set of Neighbours = " + server.knowNeighbours + '\n'
            );
            clientOut.flush();
        } catch (Exception e) {
            logger.severe("Error ocured (getServerInfo)");
        }
    }

    /**
     * Sends the Routing table of Peer
     * @param clientOut the output stream of the client
     */
    private void getRoutingTable(ObjectOutputStream clientOut) {
        // Syncronize on kademliaNode to avoid race conditions between threads
        synchronized (kademliaNode){
            try {
                SecureMessage secureMessage =
                        new SecureMessage("GET_ROUTING_TABLE",kademliaNode.getRoutingTable(),
                                miner.getPublicKey(), miner.getPrivateKey());
                //clientOut.writeObject(kademliaNode.getRoutingTable());
                clientOut.writeObject(secureMessage);
                clientOut.flush();
            }catch (Exception e){
                logger.severe("Error ocured (getRoutingTable)");
            }
        }
    }
    private void getMiner(ObjectOutputStream clientOut) {
        try{
            logger.info("Received GET_MINER");
            SecureMessage secureMessage =
                    new SecureMessage("GET_MINER",miner,miner.getPublicKey(),miner.getPrivateKey());
            clientOut.writeObject(secureMessage);
            clientOut.flush();
        } catch (Exception e){
            logger.severe("Error ocured (getServerObject)");
            e.printStackTrace();
        }

    }
}
