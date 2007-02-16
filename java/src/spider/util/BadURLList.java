package spider.util;

import java.util.Hashtable;

/**
 * a list of URLs that do not repond properly to HTTP (text/html) request
 *
 * @author gpant
 */
public class BadURLList {
    private static Hashtable list = new Hashtable();

    public synchronized static void addElement(String URL) {
        if (URL != null) {
            list.put(URL, "");
        }
    }

    public synchronized static boolean isBadURL(String URL) {
        if (URL != null) {
            if (list.containsKey(URL)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

}
