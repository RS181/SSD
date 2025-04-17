package P2P;

import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

/**
 * Class that creates or loads Priv. and Pub. keys
 * associated with a Peer
 */
public class KeysUtils {

    private static final String KEYS_DIR = "./src/main/java/P2P/PeersKeys/";

    public static KeyPair loadOrCreateKeyPair(String host, int port) throws Exception {
        String prefix = host + "_" + port;
        Path privateKeyPath = Paths.get(KEYS_DIR + prefix + "_PRK");
        Path publicKeyPath = Paths.get(KEYS_DIR + prefix + "_PKK");

        Files.createDirectories(Paths.get(KEYS_DIR));

        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);
            PublicKey publicKey = loadPublicKey(publicKeyPath);
            return new KeyPair(publicKey, privateKey);
        } else {
            return generateAndStoreKeyPair(privateKeyPath, publicKeyPath);
        }
    }

    private static KeyPair generateAndStoreKeyPair(Path privPath, Path pubPath) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Store in binary
        Files.write(privPath, keyPair.getPrivate().getEncoded());
        Files.write(pubPath, keyPair.getPublic().getEncoded());

        return keyPair;
    }

    public static PrivateKey loadPrivateKey(Path path) throws Exception {
        byte[] keyBytes = Files.readAllBytes(path);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(Path path) throws Exception {
        byte[] keyBytes = Files.readAllBytes(path);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
