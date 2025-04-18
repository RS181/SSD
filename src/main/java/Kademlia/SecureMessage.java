package Kademlia;

import Cryptography.CryptoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Message class for S/Kademlia
 */
public class SecureMessage implements Serializable {
    private String command;                 // Ex: "STORE", "FIND_NODE", etc
    private Object payload;                 // Any object (Block, Node, etc)
    private PublicKey senderPublickKey;     // Sender's public key
    private byte[] signature;               // Signature of the content

    /**
     * Constructor for SecureMessage
     * @param command           type of message
     * @param payload           content of message
     * @param senderPublickKey  sender's public key
     * @param senderPrivateKey  sender's private key
     */
    public SecureMessage(String command, Object payload, PublicKey senderPublickKey, PrivateKey senderPrivateKey){
        this.command = command;
        this.payload = payload;
        this.senderPublickKey = senderPublickKey;
        this.signature = signPayload(senderPrivateKey);
    }

    /* Getter's  & setter's (the setter's are only here fo testing purposes)*/
    public Object getPayload() {
        return payload;
    }

    public PublicKey getSenderPublickKey() {
        return senderPublickKey;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }


    /* Auxiliar methods */

    /**
     * Digitally signs the message content (composed of the command and serialized payload) using the provided private key.
     *
     * <p>This method concatenates the {@code command} and the serialized {@code payload} into a single byte array,
     * which is then signed using {@link CryptoUtils#sign(PrivateKey, byte[])}. The resulting signature ensures the
     * integrity and authenticity of the message.</p>
     *
     * @param privateKey    the sender's private key used to generate the digital signature
     * @return              a byte array representing the digital signature of the message content
     *
     * @throws RuntimeException if an I/O error occurs while serializing the message content
     *
     * @implNote The signature is computed over the command string and the payload data. Any tampering with
     *           the message after signing will invalidate the signature, enabling secure message verification.
     */
    private byte[] signPayload(PrivateKey privateKey) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(command.getBytes(StandardCharsets.UTF_8));
            outputStream.write(CryptoUtils.serialize(payload));
            byte[] contentToSign = outputStream.toByteArray();

            return CryptoUtils.sign(privateKey, contentToSign);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies the digital signature of this message using the sender's public key.
     *
     * <p>This method reconstructs the original message content (command and serialized payload)
     * and uses {@link CryptoUtils#verifySignature(PublicKey, byte[], byte[])} to verify whether the
     * provided signature matches the expected one. It ensures that the message was not altered and
     * was signed by the legitimate sender.</p>
     *
     * @return {@code true} if the signature is valid; {@code false} otherwise
     *
     * @throws RuntimeException if an I/O error occurs during content serialization
     *
     * @implNote This method is crucial for maintaining trust in the Kademlia network by protecting
     *           against forged messages and impersonation attacks.
     */
    public boolean verifySignature() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(command.getBytes(StandardCharsets.UTF_8));
            outputStream.write(CryptoUtils.serialize(payload));
            byte[] contentToVerify = outputStream.toByteArray();

            return CryptoUtils.verifySignature(senderPublickKey, contentToVerify, signature);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "SecureMessage{" +
                "command='" + command + '\'' +
                ", payload=" + payload +
                ", senderPublicKey=" + senderPublickKey +
                ", signature=" + signature +
                '}';
    }



}
