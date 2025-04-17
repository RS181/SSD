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


    public SecureMessage(String command, Object payload, PublicKey senderPublickKey, PrivateKey senderPrivateKey){
        this.command = command;
        this.payload = payload;
        this.senderPublickKey = senderPublickKey;
        this.signature = signPayload(senderPrivateKey);
    }

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
}
