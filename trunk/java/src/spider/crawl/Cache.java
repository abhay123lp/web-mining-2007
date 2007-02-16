package spider.crawl;

import java.io.File;
import java.io.FileWriter;

/**
 * @author Gautam Pant
 *         <p/>
 *         Used for caching and keeping tack of cached elements
 */
public class Cache {

    /**
     * The path where cache files need to be stored
     */
    private String path = "";

    /**
     * extra cache path required when the cache is split into subdirectories
     */
    private boolean extraCachePath = true;

    /**
     * To add the content of a url to cache
     */
    public synchronized boolean addToCache(String url, String filename, String content, long lastModified) {
        url = url.trim();
        try {
            // if file does not exists in cache
            File f = new File(getPath(filename) + filename);
            if (!f.exists()) {
                File dir = new File(getPath(filename));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileWriter fw = new FileWriter(f);
                fw.write(content);
                fw.flush();
                fw.close();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * deletes all the files in the current cache
     * assumes a maximum of two level deep directory structure
     */
    public synchronized void clearCache() {
        if (path.equals("")) {
            return;
        }
        File dir = new File(path);
        if (dir.exists()) {
            System.out.print("Clearing the cache...");
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    File[] subFiles = files[i].listFiles();
                    for (int j = 0; j < subFiles.length; j++) {
                        subFiles[i].delete();
                    }
                } else {
                    files[i].delete();
                }
            }
            System.out.println("done");
        }
    }

    /**
     * Returns the path.
     *
     * @return String
     */
    public String getPath(String fileName) {
        String extraPath = "";
        if (extraCachePath) {
            extraPath = fileName.substring(0, 3) + "/";
        }
        String completePath = path + extraPath;
        //System.out.println(completePath);
        return completePath;
    }

    /**
     * Sets the path.
     *
     * @param path The path to set
     */
    public void setPath(String path) {
        if (!path.endsWith("/")) {
            StringBuffer sb = new StringBuffer(path);
            sb.append("/");
            path = sb.toString();
        }
		this.path = path;
	}

}
