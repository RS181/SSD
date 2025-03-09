package BlockChain;

import Cryptography.CryptoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class with Tests relating to Blockchain (Transactions are jus String's
 * in this tests
 *
 * TODO: ajustar transações para usar classe Transactions
 */
class BlockchainTest {

    private  Blockchain blockchain;
    private  Miner m = new Miner();
    private  Miner m2 = new Miner();
    private  String[] initialValues = {"Shad has $700", "Miguel has $550"};
    private  String[] shadGivesItAway = {"Shad gives Tim $40" , "Shad gives Tany $60" ,"Shad gives Terry $100"};

    private  String[] shadGetsSome = {"Tim gives Shad $10" , "Terry gives $50 to Shad" };

    @BeforeEach
    void setup(){
        blockchain = new Blockchain();
    }

    @Test
    void addValidBlocks(){
        Block firstBlock = m.mineBlock(initialValues,"");
        assertTrue(blockchain.addBlock(firstBlock,m),"Erro na adição do pimeiro bloco");

        Block secondBlock = m.mineBlock(shadGivesItAway,firstBlock.getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock,m),"Erro na adição do segundo bloco");

        Block thirdBlock = m.mineBlock(shadGetsSome,secondBlock.getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock,m),"Erro na adição do terceiro bloco");

        assertTrue(blockchain.checkCurrentChain());
    }

    @DisplayName("Try to add  Block that wasn't properly mined (Empty Blockchain)")
    @Test
    void checkBlockPow_1(){
        Block firstBlock = new Block(initialValues,"");
        assertFalse(blockchain.addBlock(firstBlock,m),"Erro não verificou corretamente PoW de um  bloco antes de adiciona-lo a Blockchain");
    }

    @DisplayName("Try to add  Block that wasn't properly mined (Blockchain with 1 Block)")
    @Test
    void checkBlockPow_2(){
        //Add First valid mined block
        Block firstBlock = m.mineBlock(initialValues,"");
        assertTrue(blockchain.addBlock(firstBlock,m),"Erro na adição do pimeiro bloco");

        //Add Second invalid block
        Block secondBlock = new Block(shadGivesItAway,firstBlock.getBlockHash());
        assertFalse(blockchain.addBlock(secondBlock,m),"Erro não verificou corretamente PoW de um  bloco antes de adiciona-lo a Blockchain");
    }

    @DisplayName("Add valid Block, and change the original miner signature")
    @Test
    void checkInvalidMinerSignature(){
        // Add First valid mined block
        Block firstBlock = m.mineBlock(initialValues,"");

        String blockHeader =
                firstBlock.getBlockHash() + firstBlock.getPreviousBlockHash() + firstBlock.getNonce() + firstBlock.getTimestamp();
        byte[] signature = firstBlock.getMinerSignature();
        assertNotNull(signature,"Erro assinatura do miner no bloco é igual a NULL");

        // Check if the block signature is valid
        boolean checkBlock =
                CryptoUtils.verifySignature(m.publicKey,blockHeader.getBytes(),signature);
        assertTrue(checkBlock,"Erro assinatura do miner é invalida, mas devia ser válida");

        // Signs the block header with other miner's priv. key
        byte[] fakesignature =
                CryptoUtils.sign(m2.getPrivateKey(),blockHeader.getBytes());

        // Puts the other miner signature in the block (which is invalid)
        firstBlock.forceSetMinerSignature(fakesignature);
        signature = firstBlock.getMinerSignature();
        checkBlock =
                CryptoUtils.verifySignature(m.publicKey,blockHeader.getBytes(),signature);
        assertFalse(checkBlock,"Erro verificação da assinatura devia ser inválida");

    }
    
    @DisplayName("Changes a random block Signature in the Block chain, and tries to add a new valid Block")
    @Test
    void changeBlockSignatureInBlockchain(){
        //Create a Blockchain with valid blocks ( mined by diferent miner's)
        Block firstBlock = m.mineBlock(initialValues,"");
        assertTrue(blockchain.addBlock(firstBlock,m),"Erro na adição do pimeiro bloco");


        Block secondBlock = m2.mineBlock(shadGetsSome,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock,m2),"Erro na adição do segundo bloco");

        Block thirdBlock = m2.mineBlock(shadGivesItAway,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock,m2), "Erro na adição do terceiro bloco");

        // Change the Miner's signature of a Block in the Blockchain
        String secondblockHeader =
                secondBlock.getBlockHash() + secondBlock.getPreviousBlockHash() + secondBlock.getNonce() + secondBlock.getTimestamp();
        byte[] fakesignature =
                CryptoUtils.sign(m.getPrivateKey(),secondblockHeader.getBytes());

        secondBlock.forceSetMinerSignature(fakesignature);

        // Try to add new Valid block to a tampered blockchain
        Block fourthBlock = m.mineBlock(shadGivesItAway,blockchain.getLastBlock().getBlockHash());
        assertFalse(blockchain.addBlock(fourthBlock,m),"Erro Blockchain tem bloco invalido, e adicionou bloco");


        System.out.println(blockchain);
        assertFalse(blockchain.checkCurrentChain(),"Erro Blockchain tem bloco invalido mas checkCurrentChain diz que é valido");
    }


    @DisplayName("Changes block hash in the Block chain, and checks if the chain is valid")
    @Test
    void changeBlockHashInBlockChain(){
        //Create a Blockchain with valid blocks ( mined by diferent miner's)
        Block firstBlock = m.mineBlock(initialValues,"");
        assertTrue(blockchain.addBlock(firstBlock,m),"Erro na adição do pimeiro bloco");


        Block secondBlock = m2.mineBlock(shadGetsSome,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock,m2),"Erro na adição do segundo bloco");

        Block thirdBlock = m2.mineBlock(shadGivesItAway,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock,m2));

        assertTrue(blockchain.checkCurrentChain(),"Erro checkCurrentChain devia retornar verdadeiro");

        // Change the block hash of first block in the blockchain
        firstBlock.setBlockHash(CryptoUtils.getHash("INVALID"));

        //secondBlock.setBlockHash(CryptoUtils.getHash("INVALID"));
        //thirdBlock.setBlockHash(CryptoUtils.getHash("INVALID"));
        assertFalse(blockchain.checkCurrentChain());

    }

    @Test
    void changeBlockPrevHashInBlockChain(){
        //Create a Blockchain with valid blocks ( mined by diferent miner's)
        Block firstBlock = m.mineBlock(initialValues,"");
        assertTrue(blockchain.addBlock(firstBlock,m),"Erro na adição do pimeiro bloco");

        Block secondBlock = m2.mineBlock(shadGetsSome,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(secondBlock,m2),"Erro na adição do segundo bloco");

        Block thirdBlock = m2.mineBlock(shadGivesItAway,blockchain.getLastBlock().getBlockHash());
        assertTrue(blockchain.addBlock(thirdBlock,m2));

        assertTrue(blockchain.checkCurrentChain(),"Erro checkCurrentChain devia retornar verdadeiro");

        // Change the previous block hash of third block in the blockchain
        thirdBlock.setPreviousBlockHash(CryptoUtils.getHash("INVALID"));

        assertFalse(blockchain.checkCurrentChain(),"Erro checkCurrentChain devia retornar falso");

    }



    //TODO adicionar restantes casos
}