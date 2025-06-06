package Cryptography;

import BlockChain.Utils;
import Kademlia.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Class with utilitaries for criptography
 */
public class CryptoUtils {
    /**
     * Sign's the data array with the private key
     *
     * @param  privateKey of the signer
     * @param  data that is going to be signed
     * @return the signed data
     *
     * @see <a href="https://www.baeldung.com/java-digital-signature">Baeldung tutorial</a>
     */
    public static byte[] sign(PrivateKey privateKey, byte[] data){
        Signature signature = null;
        try {
            signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies the digital signature using the given public key.
     *
     * @param  publicKey the public key used for verification
     * @param  data the original data that was signed
     * @param  signature the digital signature to be verified
     * @return {@code true} if the signature is valid, {@code false} otherwise
     */

    public static boolean verifySignature(PublicKey publicKey, byte[] data, byte[] signature){
        Signature verifier = null;
        try {
            verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(data);
            return verifier.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates the SHA-256 hash of a given input string.
     *
     * @param  input the string to be hashed
     * @return the hash of the input as a hexadecimal string
     */
    public static String getHash256(String input){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Utils.getHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates the SHA-1 hash of a given input string.
     *
     * @param  input the string to be hashed
     * @return the hash of the input as a hexadecimal string
     */
    public static String getHash1(String input){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Utils.getHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a hexadecimal string representation into a byte array.
     *
     * @param  s the hexadecimal string to convert
     * @return a byte array representing the hexadecimal string
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Serializes an object into a byte array.
     *
     * @param  obj the object to be serialized
     * @return a byte array representing the serialized object
     * @throws IOException if an I/O error occurs during serialization
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        return baos.toByteArray();
    }

    /**
     * Generates a secure node ID based on the SHA-256 hash of the provided public key.
     * The resulting ID is a binary string with a length defined by {@link Kademlia.Constants#NUMBER_OF_BITS_NODE_ID}.
     *
     * @param  publicKey the public key to generate the node ID from
     * @return a binary string representing the secure node ID
     */
    public static String generateSecureNodeId(PublicKey publicKey){
        String hashHex = CryptoUtils.getHash256(String.valueOf(publicKey)); // SHA-256 hash in hex
        return generateBinaryId(hashHex);
    }

    /**
     * Generates a key ID based on the SHA-256 hash of the provided key string.
     * The resulting ID is a binary string with a length defined by {@link Kademlia.Constants#NUMBER_OF_BITS_NODE_ID}.
     *
     * @param  key the key string to generate the key ID from
     * @return a binary string representing the key ID
     */
    public static String generateKeyId(String key){
        String hashHex = CryptoUtils.getHash256(key);
        return generateBinaryId(hashHex);
    }

    /**
     * Converts a hexadecimal hash string into a binary string of a predefined length.
     * The length of the binary string is determined by {@link Kademlia.Constants#NUMBER_OF_BITS_NODE_ID}.
     *
     * @param  hashHex the hexadecimal hash string to convert
     * @return a binary string representation of the hash, truncated to the required number of bits
     * @throws IllegalArgumentException if the hash output is too short for the required number of bits
     */
    private static String generateBinaryId(String hashHex) {
        byte[] hashBytes = CryptoUtils.hexStringToByteArray(hashHex); // Convert hex string to bytes
        int numberOfBits = Constants.NUMBER_OF_BITS_NODE_ID;
        int numberOfBytesNeeded = (int) Math.ceil(numberOfBits / 8.0);

        if (hashBytes.length < numberOfBytesNeeded) {
            throw new IllegalArgumentException("Hash output is too short for the required number of bits");
        }

        StringBuilder binaryBuilder = new StringBuilder();
        for (int i = 0; i < numberOfBytesNeeded; i++) {
            binaryBuilder.append(String.format("%8s", Integer.toBinaryString(hashBytes[i] & 0xFF)).replace(' ', '0'));
        }

        // Truncate to the exact number of bits required
        return binaryBuilder.substring(0, numberOfBits);
    }
}
