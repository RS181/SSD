package Cryptography;

import BlockChain.Utils;

import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Class with utilitaries for criptography
 */
public class CryptoUtils {

    /**
     * Sign's the data array with the private key
     *
     * @param privateKey of the signer
     * @param data that is going to be signed
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
     * @param publicKey the public key used for verification
     * @param data the original data that was signed
     * @param signature the digital signature to be verified
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
     * @param input the string to be hashed
     * @return the hash of the input as a hexadecimal string
     */
    public static String getHash(String input){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Utils.getHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


}
