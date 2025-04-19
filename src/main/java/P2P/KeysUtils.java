package P2P;

import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

/**
 * Class that creates or loads Priv. and Pub. keys
 * associated with a Peer
 */
public class KeysUtils {

    /**
     * Directory where the peer's private and public keys are stored.
     */
    public static final String KEYS_DIR = "./src/main/java/P2P/PeersKeys/";


    /**
     * Loads an existing key pair from the filesystem or creates a new one if the keys do not exist.
     *
     * @param  host the host of the peer
     * @param  port the port of the peer
     * @return the key pair (public and private keys)
     * @throws Exception if an error occurs while loading or creating the key pair
     */
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

    /**
     * Generates and stores a new RSA key pair (private and public keys).
     *
     * @param  privPath the path to store the private key
     * @param  pubPath the path to store the public key
     * @return the generated key pair (public and private keys)
     * @throws Exception if an error occurs during key generation or storage
     */
    private static KeyPair generateAndStoreKeyPair(Path privPath, Path pubPath) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Store in binary
        Files.write(privPath, keyPair.getPrivate().getEncoded());
        Files.write(pubPath, keyPair.getPublic().getEncoded());

        return keyPair;
    }

    /**
     * Loads a private key from the specified file path.
     *
     * @param  path the path to the private key file
     * @return the loaded private key
     * @throws Exception if an error occurs while loading the private key
     */
    private static PrivateKey loadPrivateKey(Path path) throws Exception {
        byte[] keyBytes = Files.readAllBytes(path);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * Loads a public key from the specified file path.
     *
     * @param  path the path to the public key file
     * @return the loaded public key
     * @throws Exception if an error occurs while loading the public key
     */
    public static PublicKey loadPublicKey(Path path) throws Exception {
        byte[] keyBytes = Files.readAllBytes(path);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
