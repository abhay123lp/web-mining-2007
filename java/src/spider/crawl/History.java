package spider.crawl;

import spider.util.Helper;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * helps to maintain history of a crawl with timestamps
 *
 * @author Gautam Pant
 */
public class History {

    Hashtable ht = new Hashtable();

    /**
     * add into history
     */
    public boolean add(String url, String filename, double score) {
        HistoryElement r = new HistoryElement();
        r.url = url;
        r.file = filename;
        r.score = score;
        r.time = System.currentTimeMillis();
        synchronized (this) {
        	ht.put(url, r);
        }
        return true;
    }

    /**
     * find the size of history (pages crawled)
     */
    public int size() {
    	synchronized (this) {
    		return ht.size();
    	}
    }

    /**
     * find if a url is in history
     */
    public boolean isInHistory(String url) {
    	synchronized (this) {
    		return ht.containsKey(url);
    	}
    }

    /**
     * returns filename that stores the history file
     */
    public String getFileName(String url) {
    	synchronized (this) {
    		if (ht.containsKey(url)) {
    			HistoryElement he = (HistoryElement) ht.get(url);
    			return he.file;
    		}
            return null;
    	}
    }

    /**
     * returns score for a given url
     */
    public double getFileScore(String url) {
    	synchronized (this) {
    		if (ht.containsKey(url)) {
    			HistoryElement he = (HistoryElement) ht.get(url);
    			return he.score;
    		}
    		return -1;
    	}
    }

    /**
     * return HistoryElement for a given url
     */
    public HistoryElement getHistoryElement(String url) {
    	synchronized (this) {
    		if (ht.containsKey(url)) {
    			HistoryElement he = (HistoryElement) ht.get(url);
    			return he;
    		}
    		return null;
    	}
    }

    /**
     * put the history into a file after sorting by time
     */
    public boolean toFile(String filename) {
        try {
            String[] parts = filename.split("/+|\\+");
            String path = Helper.join("/", parts, 0, parts.length - 2);
            File f = new File(path);
        	synchronized (this) {
        		if (!f.exists()) {
        			f.mkdir();
        		}
        		FileWriter fw = new FileWriter(new File(filename));
        		Vector list = new Vector();
        		for (Enumeration e = ht.keys(); e.hasMoreElements();) {
        			HistoryElement u = (HistoryElement) ht.get(e.nextElement());
        			list.add(u);
        		}
        		//sort the list by timestamp
        		Collections.sort(list);
        		for (int i = 0; i < list.size(); i++) {
        			HistoryElement he = (HistoryElement) list.get(i);
        			fw.write(i + "  " + he.url + "  " + he.file + "  " + he.score + "\n");
        		}
        		fw.flush();
        		fw.close();
        	}
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * inner class to record history data
     */
    public class HistoryElement implements Comparable {
        String url = null;
        String file = null;
        double score = -1;
        long time = 0;

        public int compareTo(Object o) {
            HistoryElement he = (HistoryElement) o;
            if (this.time > he.time) {
                return 1;
            } else if (this.time == he.time) {
                return 0;
            }
            return -1;
        }
    }

    /**
     * Returns the ht.
     *
     * @return Hashtable
     */
    public Hashtable getHistory() {
        return ht;
    }


}
