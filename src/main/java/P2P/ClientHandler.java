package P2P;

import BlockChain.Block;
import BlockChain.Blockchain;
import BlockChain.Miner;
import BlockChain.Transaction;
import Cryptography.CryptoUtils;
import Kademlia.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.PublicKey;
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
                    case "IS_ON_NETWORK": // We considered that the node is on the network (if it has at least 2 neighbours)
                        if (server.knowNeighbours.size() >= 2)
                            out.writeObject( "OK: Peer is part of network" );
                        else
                            out.writeObject( "NOT OK: Peer is not part of network" );
                        out.flush();
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
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Caught IOException or ClassNotFoundException in ClientHandler (run)");
            //e.printStackTrace();
        }
    }

    /**
     * Stores a received block in the local Kademlia node's storage.
     */
    private void addToStorageHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        logger.info("Received Add to 'kademlia' node's Storage");
        // Syncronize on kademlia Node to avoid race conditions between threads
        synchronized (kademliaNode) {
            try {
                clientOut.writeObject("OK from @ " + server.host + " " + server.port);
                clientOut.flush();

                Object receivedObject = clientIn.readObject();
                
                if (receivedObject instanceof Block b ){
                    logger.info("Received Block to add to 'kademlia' node's Storage = [" + b.getBlockHash() + "]");
                    String keyId = CryptoUtils.generateKeyId(b.getBlockHash());
                    kademliaNode.addToLocalStorage(keyId,b);
                    clientOut.writeObject("Updated Kademlia Node storage \n:" + kademliaNode.getLocalStorage());
                }else {
                    clientOut.writeObject("Error: Expected Block but received something else");
                    logger.warning("Error: Did not receive a Block (addToStorageHandler)");
                }
            }
            catch (Exception e){
                logger.severe("Error ocured (addToStorageHandler)");
            }
        }
    }


    /**
     * Handles a FIND_NODE request and responds with the closest known nodes.
     */
    private void findNodeHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on kademlia Node to avoid race conditions between threads
        synchronized (kademliaNode) {
            try {
                logger.info("Received Find Node message");
                clientOut.writeObject( "OK from @ " + server.host + " " + server.port );
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof String targetNodeId) {
                    logger.info( "Receive FIND_NODE(" + targetNodeId + ")" );
                    List<Node> kClosestNodes =
                            kademliaNode.getRoutingTable().getClosestNodes( Constants.MAX_RETURN_FIND_NODES, targetNodeId );

                    SecureMessage secureMessage = new SecureMessage( "FIND_NODE", kClosestNodes
                            , miner.getPublicKey(), miner.getPrivateKey() );

                    clientOut.writeObject( secureMessage );
                } else {
                    clientOut.writeObject( "Error: Expected Node Id but received something else" );
                    logger.warning( "Error: Did not receive a Node Id (findNodeHandler)" );
                }
            } catch (Exception e) {
                logger.severe("Error ocured (findNodeHandler)");
            }
        }
    }

    /**
     * Handles a FIND_VALUE request, returning the value or closest nodes.
     */
    private void findValueHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on kademlia Node to avoid race conditions between threads
        synchronized (kademliaNode) {
            try {
                logger.info("Received Find Value message");
                clientOut.writeObject( "OK" );
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof String keyId) {
                    Block b = kademliaNode.getValue( keyId );
                    if (b != null) {
                        logger.info( "Value found in local storage" );
                        SecureMessage secureMessage =
                                new SecureMessage( "FIND_VALUE", b,
                                        miner.getPublicKey(), miner.getPrivateKey() );
                        clientOut.writeObject( secureMessage );
                    } else {
                        logger.warning( "Value not found in local storage" );
                        List<Node> closestNodestoKey =
                                kademliaNode.getRoutingTable().getClosestNodes
                                        ( Constants.MAX_ROUTING_FIND_NODES, keyId );
                        SecureMessage secureMessage =
                                new SecureMessage( "FIND_VALUE", closestNodestoKey,
                                        miner.getPublicKey(), miner.getPrivateKey() );
                        clientOut.writeObject( secureMessage );
                    }
                } else {
                    clientOut.writeObject( "Error: Expected String but received something else" );
                    logger.warning( "Error: Did not receive a String (findValueHandler)" );
                }
            } catch (Exception e){
                logger.severe("Error ocured (findValueHandler)");
            }
        }
    }

    /**
     * Handles a PING request to verify node liveness and identity.
     */
    private void pingHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        try{
            logger.info("Received Ping ...");
            clientOut.writeObject("OK");
            clientOut.flush();

            Object receivedObject = clientIn.readObject();

            if (receivedObject instanceof  SecureMessage secureMessage
                && secureMessage.verifySignature()
                && secureMessage.getPayload() instanceof Node n
                && Operations.checkNodeId(secureMessage, n))
            {
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

    /**
     * Handles a STORE request to save a key-value pair in the local storage.
     */
    private void storeHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        // Syncronize on kademliaNode to avoid race conditions between threads
        synchronized (kademliaNode){
            try {
                logger.info("Received Store <Key,Value> message");
                clientOut.writeObject("Ok");
                clientOut.flush();

                Object receivedObject = clientIn.readObject();

                if (receivedObject instanceof SecureMessage secureMessage
                        && secureMessage.verifySignature()
                        && secureMessage.getPayload() instanceof BlockKeyWrapper blockKeyWrapper)
                {
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
     * Handles the removal of a peer from the server’s known neighbors.
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
     * Handles the addition of a peer to the server’s known neighbors.
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
     * Handles the mining process and communicates the result to the client.
     *
     * <p>This method ensures that only valid transactions are mined by calling {@code removeInvalidPlaceBid()}
     * before proceeding. If there are no valid transactions left, an informative message is sent to the client.</p>
     *
     * <p>Otherwise, a new block is mined from the current state of the transaction pool, then validated,
     * added to the local blockchain, and propagated to peers in the network.</p>
     *
     * <p>Steps performed after successful mining:
     * <ul>
     *   <li>The transaction pool is cleared to prevent reuse of already-mined transactions.</li>
     *   <li>A "STOP" message is sent to all known neighbors to halt their ongoing mining operations.</li>
     *   <li>An "ADD_MINED_BLOCK" message is sent to ensure neighbors update their blockchains with the new block.</li>
     *   <li>A "STORE" operation is triggered to distribute and persist the block across the network.</li>
     * </ul>
     *
     * <p>Note: This method performs synchronization only when accessing or modifying the {@code transactionsPool},
     * to ensure thread safety and prevent race conditions in a multithreaded environment. Time-consuming operations
     * such as mining and networking are executed outside the synchronized block to avoid blocking other threads.</p>
     *
     * @param clientOut the stream used to send responses to the client
     */
    private void mineHandler(ObjectOutputStream clientOut) {
        ArrayList<Transaction> transactionsToMine;
        synchronized (transactionsPool) {
            removeInvalidPlaceBid();
            if (transactionsPool.isEmpty() ) {
                try {
                    clientOut.writeObject("Dont have enough transactions to mine a block");
                    clientOut.flush();
                } catch (IOException e) {
                    logger.warning("I/O error while replying to client");
                }
                return;
            }
            transactionsToMine = new ArrayList<>(transactionsPool); // clone current pool
        }

        try {
            miner.canStartMining();
            logger.info("Received MINE message");

            String prevhash = blockchain.getLastBlock() != null ? blockchain.getLastBlock().getBlockHash() : "";

            logger.info("Started Mining Block ...");
            Block b = miner.mineBlock(transactionsToMine, prevhash);
            logger.info("Finished Mining Block !!!");

            if (!client.isClosed()) {
                if (!blockchain.addBlock(b, miner.publicKey)) {
                    logger.severe("Error occurred while adding block to blockchain (mineHandle)");
                    return;
                }

                // Reset transactions pool (with syncronized, to avoid race conditions)
                synchronized (transactionsPool) {
                    logger.info("Resetting Transactions pool");
                    transactionsPool.clear();
                }

                clientOut.writeObject("OK");
                clientOut.flush();

                for (Node n : server.knowNeighbours) {
                    PeerComunication.sendMessageToPeer(n.getIpAddr(), n.getPort(), "STOP", null);
                    PeerComunication.sendMessageToPeer(n.getIpAddr(), n.getPort(), "ADD_MINED_BLOCK", b);
                }

                Node sender = new Node(server.host, server.port, false);
                Operations.store(sender, b.getBlockHash(), b, miner);
            } else {
                System.out.println("Client Socket is closed");
            }
        } catch (SocketException e) {
            logger.warning("Socket Closed while peer was mining (mineHandler)");
        } catch (IOException | NullPointerException e) {
            logger.warning("Exception in mineHandler");
            e.printStackTrace();
        }
    }

    /**
     * Remove PLACE_BID transaction that are no longer valid
     * (e.g. the auction the bid was made is closed)
     */
    private void removeInvalidPlaceBid(){
        Iterator<Transaction> it = transactionsPool.iterator();
        while (it.hasNext()) {
            Transaction t = it.next();
            if (t.getType().equals( Transaction.TransactionType.PLACE_BID  )) {
                Set<String> availableAuctions = blockchain.getAvailableAuctions();
                String auctionId = t.getAuctionId();
                for(String auction : availableAuctions){
                    if(auction.equals(auctionId))
                        return;
                }
                it.remove();
            }
        }
    }

    /**
     * Handles the reception of a mined block from a peer and adds it to the blockchain.
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
                        clientOut.writeObject("Error: Expected Block but received something else");
                        logger.warning("Error: Expected Block but received something else (addMinedBlockHandler)");
                    }
                }catch (Exception e){
                    logger.severe("Error ocured (addMinedBlockHandler)");
                }
        }
    }

    /**
     * Handles the reception and processing of a new transaction from a client.
     *
     * <p>The transaction is first validated using {@code checkTransaction(t, t.getType())}. If valid, it is added
     * to the {@code transactionsPool} according to specific rules based on its type:</p>
     *
     * <ul>
     *   <li>For standard transactions (not {@code PLACE_BID} or {@code CLOSE_AUCTION}): added if the pool has less than 3.</li>
     *   <li>For {@code PLACE_BID} or {@code CLOSE_AUCTION} transactions: added regardless of pool size, but they
     *       will immediately trigger mining if added to an empty pool.</li>
     * </ul>
     *
     * <p>If the conditions for mining are met (e.g. pool reaches 3 transactions, or a bid-related transaction is added
     * when the pool is empty), a "MINE" request is sent to the local peer, which initiates the mining process asynchronously.</p>
     *
     * <p>This method uses a {@code synchronized} block around the {@code transactionsPool} to ensure thread safety when
     * reading and modifying it. Any potentially blocking or long operations, such as initiating mining or logging, are
     * executed outside the synchronized block to avoid deadlocks or thread contention.</p>
     *
     * @param clientIn  the input stream to receive the transaction object from the client
     * @param clientOut the output stream to send responses back to the client
     */

    private void addTransactionHandler(ObjectInputStream clientIn, ObjectOutputStream clientOut) {
        boolean shouldMine = false;
        String mineReason = "";     // only for debug

        try {
            logger.info("Adding transaction ...");
            clientOut.writeObject("OK");
            clientOut.flush();

            Object receivedObject = clientIn.readObject();

            if (receivedObject instanceof Transaction t && checkTransaction(t, t.getType())) {
                synchronized (transactionsPool) {
                    if (!t.getType().equals(Transaction.TransactionType.PLACE_BID) &&
                        !t.getType().equals(Transaction.TransactionType.CLOSE_AUCTION)) {

                        if (transactionsPool.size() < 3) {
                            transactionsPool.add(t);
                            if (transactionsPool.size() == 3) {
                                shouldMine = true;
                                mineReason = "Transaction pool reached 3 transactions";
                            }
                        }
                    } else {
                        transactionsPool.add(t);
                        shouldMine = true;
                        mineReason = "PLACE_BID or CLOSE_AUCTION added with empty pool";
                    }
                }

                clientOut.writeObject("OK");

                // Outside of  synchronized!
                if (shouldMine) {
                    logger.info("Triggering mining process because: " + mineReason);
                    String response =
                            (String) PeerComunication.sendMessageToPeer(server.host, server.port, "MINE", null);
                    System.out.println("Peer [MINE Triggered] Response: " + response);
                }
            } else {
                clientOut.writeObject("NOT OK: Invalid transaction");
            }
        } catch (Exception e) {
            logger.severe("Error occurred (addTransactionHandler)");
            e.printStackTrace();
        }
    }

    /**
     * Validates a transaction based on its type, checking auction start, stop, or bid placement conditions.
     */
    private Boolean checkTransaction(Transaction t, Transaction.TransactionType type){
        Boolean ans = true;
        switch (type){
            case START_AUCTION:
                System.out.println("Received START_AUCTION");
                ans = checkStartAuction(t);
                System.out.printf("Check START_AUCTION %s = %s\n",t.getAuctionId(),ans);
                break;
            case CLOSE_AUCTION:
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
     * @return  {@code true} if no auction with the same ID exists in the blockchain or transaction pool;
     *          {@code false} otherwise.
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
     * @return  {@code true} if a matching {@code START_AUCTION} transaction with the same auction ID
     *          exists in the blockchain or transaction pool; {@code false} otherwise.
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

    /**
     * Determines and announces the winner of an auction based on the highest bid.
     */
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
     * @return  {@code true} if the bid is valid (auction exists and the bid is higher than any existing bid); {@code false} otherwise
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

    /**
     * Retrieves and sends all available bids for a specific auction to the client.
     */
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
                // TODO por como securemessage
                clientOut.writeObject(transactionsPool);
                clientOut.flush();
            } catch (Exception e) {
                logger.severe("Error ocured (getTransactionPool)");
            }
        }
    }

    /**
     * Sends the current state of the blockchain to the client through the given output stream.
     * <p>
     * This method creates a {@link SecureMessage} containing the blockchain data,
     * signs it using the miner's public and private keys, and writes it to the client
     * via the provided {@link ObjectOutputStream}. The operation is synchronized on the
     * blockchain object to prevent concurrent modifications by multiple threads.
     * </p>
     *
     * @param clientOut the output stream used to send the blockchain to the client
     */
    private void getBlockchain(ObjectOutputStream clientOut) {
        // Syncronize on blockchain to avoid race conditions between threads
        synchronized (blockchain) {
            try {
                SecureMessage secureMessage =
                        new SecureMessage( "GET_BLOCKCHAIN", blockchain,
                                miner.getPublicKey(),miner.getPrivateKey());
                clientOut.writeObject(secureMessage);
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
                // TODO por como securemessage
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
                clientOut.writeObject(secureMessage);
                clientOut.flush();
            }catch (Exception e){
                logger.severe("Error ocured (getRoutingTable)");
            }
        }
    }

    /**
     * Sends the miner's information to the client.
     */
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
