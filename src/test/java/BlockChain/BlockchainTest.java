package BlockChain;

import Cryptography.CryptoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class with Tests relating to Blockchain (Transactions are jus String's
 * in this tests
 *
 * TODO: ajustar transações para usar classe Transactions
 */
class BlockchainTest {

    private  Blockchain blockchain;
    private  Miner user1 = new Miner();
    private  Miner user2 = new Miner();
    ArrayList<Transaction> startAuction = new ArrayList<>();
    ArrayList<Transaction> placeBids = new ArrayList<>();
    ArrayList<Transaction> closeAuction = new ArrayList<>();

    @BeforeEach
    void setup(){
        blockchain = new Blockchain();
        startAuction.add(new Transaction("user1","user1", Transaction.TransactionType.START_AUCTION, "AUC123", 0, System.currentTimeMillis()));
        placeBids.add(new Transaction("user2","user2", Transaction.TransactionType.PLACE_BID, "AUC123", 100.50, System.currentTimeMillis()));
        placeBids.add(new Transaction("user1","user1", Transaction.TransactionType.PLACE_BID, "AUC123", 150.75, System.currentTimeMillis()));
        closeAuction.add(new Transaction("user2","user2", Transaction.TransactionType.CLOSE_AUCTION, "AUC123", 0, System.currentTimeMillis()));

    }

    @Test
    void addValidBlocks(){
        Block firstBlock = user1.mineBlock(startAuction,"");
        assertTrue(blockchain.addBlock(firstBlock, user1.publicKey),"Erro na adição do pimeiro bloco");

        Block secondBlock = user1.mineBlock(placeBids,firstBlock.getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock, user1.publicKey),"Erro na adição do segundo bloco");

        Block thirdBlock = user1.mineBlock(closeAuction,secondBlock.getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock, user1.publicKey),"Erro na adição do terceiro bloco");

        assertTrue(blockchain.checkCurrentChain());
    }

    @DisplayName("Try to add  Block that wasn't properly mined (Empty Blockchain)")
    @Test
    void checkBlockPow_1(){
        Block firstBlock = new Block(startAuction,"");
        assertFalse(blockchain.addBlock(firstBlock, user1.publicKey),"Erro não verificou corretamente PoW de um  bloco antes de adiciona-lo a Blockchain");
    }

    @DisplayName("Try to add  Block that wasn't properly mined (Blockchain with 1 Block)")
    @Test
    void checkBlockPow_2(){
        //Add First valid mined block
        Block firstBlock = user1.mineBlock(startAuction,"");
        assertTrue(blockchain.addBlock(firstBlock, user1.publicKey),"Erro na adição do pimeiro bloco");

        //Add Second invalid block
        Block secondBlock = new Block(placeBids,firstBlock.getBlockHash());
        assertFalse(blockchain.addBlock(secondBlock, user1.publicKey),"Erro não verificou corretamente PoW de um  bloco antes de adiciona-lo a Blockchain");
    }

    @DisplayName("Add valid Block, and change the original miner signature")
    @Test
    void checkInvalidMinerSignature(){
        // Add First valid mined block
        Block firstBlock = user1.mineBlock(startAuction,"");

        String blockHeader =
                firstBlock.getBlockHash() + firstBlock.getPreviousBlockHash() + firstBlock.getNonce()
                        + firstBlock.getTimestamp() + firstBlock.getTransactions();
        byte[] signature = firstBlock.getMinerSignature();
        assertNotNull(signature,"Erro assinatura do miner no bloco é igual a NULL");

        // Check if the block signature is valid
        boolean checkBlock =
                CryptoUtils.verifySignature(user1.publicKey,blockHeader.getBytes(),signature);
        assertTrue(checkBlock,"Erro assinatura do miner é invalida, mas devia ser válida");

        // Signs the block header with other miner's priv. key
        byte[] fakesignature =
                CryptoUtils.sign(user2.getPrivateKey(),blockHeader.getBytes());

        // Puts the other miner signature in the block (which is invalid)
        firstBlock.forceSetMinerSignature(fakesignature);
        signature = firstBlock.getMinerSignature();
        checkBlock =
                CryptoUtils.verifySignature(user1.publicKey,blockHeader.getBytes(),signature);
        assertFalse(checkBlock,"Erro verificação da assinatura devia ser inválida");

    }
    
    @DisplayName("Changes a random block Signature in the Block chain, and tries to add a new valid Block")
    @Test
    void changeBlockSignatureInBlockchain(){
        //Create a Blockchain with valid blocks ( mined by diferent miner's)
        Block firstBlock = user1.mineBlock(startAuction,"");
        assertTrue(blockchain.addBlock(firstBlock, user1.publicKey),"Erro na adição do pimeiro bloco");


        Block secondBlock = user2.mineBlock(placeBids,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock,user2.publicKey),"Erro na adição do segundo bloco");

        Block thirdBlock = user2.mineBlock(closeAuction,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock,user2.publicKey), "Erro na adição do terceiro bloco");

        // Change the Miner's signature of a Block in the Blockchain
        String secondblockHeader =
                secondBlock.getBlockHash() + secondBlock.getPreviousBlockHash() + secondBlock.getNonce() + secondBlock.getTimestamp();
        byte[] fakesignature =
                CryptoUtils.sign(user1.getPrivateKey(),secondblockHeader.getBytes());

        secondBlock.forceSetMinerSignature(fakesignature);

        // Try to add new Valid block to a tampered blockchain
        Block fourthBlock = user1.mineBlock(closeAuction,blockchain.getLastBlock().getBlockHash());
        assertFalse(blockchain.addBlock(fourthBlock, user1.publicKey),"Erro Blockchain tem bloco invalido, e adicionou bloco");


        System.out.println(blockchain);
        assertFalse(blockchain.checkCurrentChain(),"Erro Blockchain tem bloco invalido mas checkCurrentChain diz que é valido");
    }


    @DisplayName("Changes block hash in the Block chain, and checks if the chain is valid")
    @Test
    void changeBlockHashInBlockChain(){
        //Create a Blockchain with valid blocks ( mined by diferent miner's)
        Block firstBlock = user1.mineBlock(startAuction,"");
        assertTrue(blockchain.addBlock(firstBlock, user1.publicKey),"Erro na adição do pimeiro bloco");


        Block secondBlock = user2.mineBlock(placeBids,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock,user2.publicKey),"Erro na adição do segundo bloco");

        Block thirdBlock = user2.mineBlock(closeAuction,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock,user2.publicKey));

        assertTrue(blockchain.checkCurrentChain(),"Erro checkCurrentChain devia retornar verdadeiro");

        // Change the block hash of first block in the blockchain
        firstBlock.setBlockHash(CryptoUtils.getHash256("INVALID"));

        //secondBlock.setBlockHash(CryptoUtils.getHash("INVALID"));
        //thirdBlock.setBlockHash(CryptoUtils.getHash("INVALID"));
        assertFalse(blockchain.checkCurrentChain());

    }

    @Test
    void changeBlockPrevHashInBlockChain(){
        //Create a Blockchain with valid blocks ( mined by diferent miner's)
        Block firstBlock = user1.mineBlock(startAuction,"");
        assertTrue(blockchain.addBlock(firstBlock, user1.publicKey),"Erro na adição do pimeiro bloco");

        Block secondBlock = user2.mineBlock(placeBids,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock,user2.publicKey),"Erro na adição do segundo bloco");

        Block thirdBlock = user2.mineBlock(closeAuction,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock,user2.publicKey));

        assertTrue(blockchain.checkCurrentChain(),"Erro checkCurrentChain devia retornar verdadeiro");

        // Change the previous block hash of third block in the blockchain
        thirdBlock.setPreviousBlockHash(CryptoUtils.getHash256("INVALID"));

        assertFalse(blockchain.checkCurrentChain(),"Erro checkCurrentChain devia retornar falso");

    }



    //TODO adicionar restantes casos
}