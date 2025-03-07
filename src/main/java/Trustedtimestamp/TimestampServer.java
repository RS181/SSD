package Trustedtimestamp;

import BlockChain.Utils;
import Cryptography.CryptoUtils;

import java.security.*;
import java.util.Date;

/**
 * Class that represents TimeStaping Authority (TSA) that is <bold>Trusted</bold>
 * as depicted in the paper "How To Time-Stamp a Digital Document"
 *
 * Follows the Singleton design Pattern
 *
 * @see <a href="https://www.youtube.com/watch?v=uAQBBZAfBL0">Digital Timestamping video</a>
 * @see <a href="https://medium.com/@1runx3na/how-to-time-stamp-a-digital-document-9c1c8535c4e2">Paper's main point's</a>
 */
public final class TimestampServer {
    private  static TimestampServer instance;
    private final PrivateKey privateKey;
    public final PublicKey publicKey;

    private TimestampServer(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public static TimestampServer getInstance() throws NoSuchAlgorithmException {
        if(instance == null){
            // Generate key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            instance = new TimestampServer(keyPair.getPrivate(),keyPair.getPublic());
        }
        return  instance;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Generates a digital timestamp document by signing the hashed client data.
     *
     * This method appends a timestamp to the provided hashed data, signs the
     * concatenated string using the private key, and returns a digital timestamp document.
     *
     * @param data the client’s hashed data (in bytes) as a string
     * @return a {@link DigitalTimeStampDocument} containing the hashed data, timestamp, and signature
     */

    public DigitalTimeStampDocument getDigitalSignature(String data) throws NoSuchAlgorithmException {

        String timestamp = Utils.convertTime(new Date().getTime());
        String concat = CryptoUtils.getHash(data + timestamp);

        byte[] signature = CryptoUtils.sign(privateKey,concat.getBytes());

        return new DigitalTimeStampDocument(data,timestamp,signature);
    }



}
