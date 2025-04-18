package Kademlia;

import BlockChain.Block;

import java.io.Serializable;

/**
 * A wrapper class that encapsulates both a {@link Block} and a corresponding key identifier.
 * <p>
 * This class is useful for transmitting a block along with its associated key (e.g., in STORE operations)
 * in a single object, simplifying communication in Kademlia-like networks.
 */
public class BlockKeyWrapper implements Serializable {
    /**
     * The key identifier associated with the block, typically used as the lookup or storage key.
     */
    private String keyId;

    /**
     * The blockchain block to be transmitted or stored.
     */
    private Block block;

    /**
     * Constructs a BlockKeyWrapper with the specified block and key ID.
     *
     * @param block the block to include in the wrapper
     * @param keyId the key identifier associated with the block
     */
    public BlockKeyWrapper(String keyId,Block block) {
        this.block = block;
        this.keyId = keyId;
    }

    /**
     * Returns the block contained in the wrapper.
     *
     * @return the block object
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Returns the key ID associated with the block.
     *
     * @return the key identifier string
     */
    public String getKeyId() {
        return keyId;
    }
}
