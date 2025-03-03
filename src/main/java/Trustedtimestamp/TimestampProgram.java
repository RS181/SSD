package Trustedtimestamp;

import Cryptography.CryptoUtils;

import java.security.NoSuchAlgorithmException;

/**
 * Class that test's the Digital timestamp of a document with a <bold>trusted</bold> TSA
 */
public class TimestampProgram {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        TimestampServer timestampServer = TimestampServer.getInstance();

        String payload = CryptoUtils.getHash("test-payload");

        DigitalTimeStampDocument t = timestampServer.getDigitalSignature(payload);

        System.out.println(t.validate("test-payload"));
    }
}
