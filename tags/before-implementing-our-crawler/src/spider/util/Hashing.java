package spider.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * uses MD5 hashing to convert arbitrary strings into a 128 bit hexadecimal (String).
 *
 * @author Gautam Pant
 */
public class Hashing {
    public static String getHashValue(String text) {
        int intHash = text.hashCode();
        // table to convert a nibble to a hex char.
        char[] hexChar = {'0', '1', '2', '3',
                '4', '5', '6', '7',
                '8', '9', 'a', 'b',
                'c', 'd', 'e', 'f'};
        //mask off the first four bytes of a long (keep the next four bytes)
        //this is to get an unsigned integer
        //long hash = intHash & 0xffffffffL;
        //return hash;
        //partial MD5 impelemtation
        byte[] buf = text.getBytes();

        MessageDigest algorithm = null;
        try {
            algorithm = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        algorithm.reset();
        algorithm.update(buf);
        byte[] b = algorithm.digest();
        //System.out.println(b.length);
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            // look up high nibble char
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);

            // look up low nibble char
            sb.append(hexChar[b[i] & 0x0f]);
        }
        //System.out.println(sb.toString().length());
        return sb.toString();
    }
}
