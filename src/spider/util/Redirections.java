package spider.util;

import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * maintains a static datastructure of redirections
 *
 * @author Gautam Pant
 */
public class Redirections {
    private static Hashtable redirect = new Hashtable();

    /**
     * add a redirection entry to the Hashtable
     */
    public synchronized static boolean addElement(String url1, String url2) {
        if (url1 != null && url2 != null) {
            redirect.put(url1, url2);
            return true;
        } else {
            return false;
        }
    }

    /**
     * write redirection data collected into the given filename
     */
    public synchronized static void toFile(String filename) {
        try {
            String[] parts = filename.split("/+|\\+");
            String path = Helper.join("/", parts, 0, parts.length - 2);
            File f = new File(path);
            if (!f.exists()) {
                f.mkdir();
            }
            FileWriter fw = new FileWriter(new File(filename));
            for (Enumeration e = redirect.keys(); e.hasMoreElements();) {
                String url1 = (String) e.nextElement();
                String url2 = (String) redirect.get(url1);
                fw.write(url1 + "  " + url2 + "\n");
            }
            fw.flush();
            fw.close();
            redirect = new Hashtable();
        } catch (Exception e) {
            return;
        }
    }

    /**
     * get the redirected location
     */
    public synchronized static String getLocation(String URL) {
        if (redirect.containsKey(URL)) {
            return (String) redirect.get(URL);
        } else {
            return null;
        }
    }

    /**
     * get size of resirections hash
     */
    public synchronized static int size() {
        return redirect.size();
	}		
}
