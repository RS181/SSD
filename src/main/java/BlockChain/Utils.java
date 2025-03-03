package BlockChain;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Class with utilitaries needed for the Blockchain*/
public class Utils {
    /**
     * Convert's a byte array into a hexadecimal string
     * @param hash
     * @return The hexadecimal string that corresponds to the byte array
     *
     * @see <a href="https://medium.com/@AlexanderObregon/what-is-sha-256-hashing-in-java-0d46dfb83888">Medium tutorial</a
     */
    public static String getHexString(byte[] hash){
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash){
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Converts a Unix time to a formatted date and time string.
     * @param time Unix time (in milliseconds)
     * @return The corresponding formated date and time String (yyyy/MM/dd HH:mm:ss)
     */
    public static  String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return format.format(date);
    }


}
