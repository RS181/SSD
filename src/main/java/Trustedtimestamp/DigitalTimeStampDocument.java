package Trustedtimestamp;

import Cryptography.CryptoUtils;

import java.security.NoSuchAlgorithmException;

/**
 * Class that represents Digital Time-Stamp document with <bold>Trusted</bold> (TSA)
 */
public class DigitalTimeStampDocument {

    String data; // hashed data
    String timestamp;
    byte[] signature;
    TimestampServer timestampServer;

    public DigitalTimeStampDocument(String data, String timestamp, byte[] signature ) throws NoSuchAlgorithmException {
        this.data = data;
        this.signature = signature;
        this.timestamp = timestamp;
        timestampServer = TimestampServer.getInstance();
    }

    /**
     * Validates the integrity and authenticity of the given data.
     *
     * This method hashes the original data, combines it with a timestamp,
     * and verifies the digital signature using the server's public key.
     *
     * @param originalData the original data that is neither signed nor hashed
     * @return {@code true} if the signature is valid, {@code false} otherwise
     */

    public  boolean validate(String originalData){
        String hash = CryptoUtils.getHash(originalData);
        String concat = CryptoUtils.getHash(hash + timestamp);
        return CryptoUtils.verifySignature(timestampServer.publicKey,concat.getBytes(),signature);
    }
}
