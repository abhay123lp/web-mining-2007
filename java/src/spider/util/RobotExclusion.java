package spider.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * stores the robot permissions for various Web sites
 * 
 * @author pant
 */
public class RobotExclusion {
    private Hashtable ht = new Hashtable();

    private int maxSize = 1000;

    private Hashtable timeStamp = new Hashtable();

    /**
     * add an entry to the robot exclusion hash (one entry per server)
     */
    public void add(String server, Vector perm) {
        synchronized (this) {
            ht.put(server, perm);
            timeStamp.put(server, new Long(System.currentTimeMillis()));

            // resize if needed
            if (ht.size() > maxSize) {
                long time = System.currentTimeMillis();
                String hostToRemove = "";
                for (Enumeration e = timeStamp.keys(); e.hasMoreElements();) {
                    String hostName = (String) e.nextElement();
                    long ts = ((Long) timeStamp.get(hostName)).longValue();
                    if (ts < time) {
                        time = ts;
                        hostToRemove = hostName;
                    }
                }
                ht.remove(hostToRemove);
                timeStamp.remove(hostToRemove);
                // System.out.println(hostToRemove+ " with timestamp: "+ time +"
                // removed at "+ System.currentTimeMillis());
            }
            if (ht.size() > maxSize || timeStamp.size() > maxSize
                    || timeStamp.size() != ht.size()) {
                System.err
                        .println("Fatal Error: Robot Exclusion cache exceeded");
                System.exit(1);
            }
        }
    }

    /**
     * get an entry for a server (a vector of disallowed paths or parts of
     * paths)
     * 
     * @return Vector or null
     */
    public Vector get(String server) {
        synchronized (this) {
            if (ht.containsKey(server)) {
                timeStamp.put(server, new Long(System.currentTimeMillis()));
                return (Vector) ht.get(server);
            } else {
                return null;
            }
        }
    }

    /**
     * a static function that gives back the robot exclusion disallow vector for
     * generic user agents given the content of a robots.txt fil
     */
    public static Vector getVector(String content) {
        Vector perm = new Vector();
        String[] lines = content.split("\n");
        Pattern p = Pattern.compile("\\s*User-agent\\s*:(.*)",
                Pattern.CASE_INSENSITIVE);
        Pattern pd = Pattern.compile("\\s*Disallow\\s*:(.*)",
                Pattern.CASE_INSENSITIVE);
        boolean add = false;
        for (int i = 0; i < lines.length; i++) {
            // if there is an empty line, it marks the end of a record
            if (lines[i].trim().length() == 0) {
                add = false;
                continue;
            }
            Matcher m = p.matcher(lines[i]);
            if (m.find()) {
                String userAgent = m.group(1);
                // if the permissions are for generic user agent
                if (userAgent.trim().equals("*")
                        || userAgent.trim().equals("Generic")) {
                    // mark the start of a relevant record
                    add = true;
                }
                // move to next line
                continue;
            }
            Matcher md = pd.matcher(lines[i]);
            if (md.find() && add) {
                String disallow = md.group(1).trim();
                if (disallow.length() > 0) {
                    perm.add(disallow);
                }
            }
        }
        return perm;
    }

    /**
     * @return - boolean - if the given url is not okay to fetch
     */
    public static boolean isDisallowed(String url, Vector perm) {
        String path = Helper.getURLPath(url);
        for (int i = 0; i < perm.size(); i++) {
            if (path.startsWith((String) perm.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the maxSize.
     *
     * @return int
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maxSize.
     *
     * @param maxSize The maxSize to set
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

}
