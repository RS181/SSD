package BlockChain;

/** Class with utilitaries needed for the Blockchain*/
public class Utils {


    /**
     *
     * Convert's a byte array into a hexadecimal string
     *
     * @param hash
     * @return
     *
     * @see <a href="https://medium.com/@AlexanderObregon/what-is-sha-256-hashing-in-java-0d46dfb83888">Medium</a
     */
    static String getHexString(byte[] hash){
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash){
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
