package Cryptography;
import java.security.*;

/**
 * Class that is used to test CryptoUtils
 *
 * @see <a href="https://medium.com/javarevisited/cryptography-sign-payload-encrypt-a-plain-text-password-and-decrypt-it-61a2d8a09e73">Medium tutorial</a>
 */
public class CryptoProgram  {

    public static void main(String[] args) throws NoSuchAlgorithmException {

        String payload = "test-payload";
        // Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();


        // Sign the payload
        byte[] signedPayload = CryptoUtils.sign(privateKey,payload.getBytes());

        // Verify the Signature
        boolean verified = CryptoUtils.verifySignature(publicKey,payload.getBytes(),signedPayload);

        if (verified) {
            // Signature is valid
            System.out.println("valid");
        } else {
            // Signature is invalid
            System.out.println("invalid");
        }

    }


}
