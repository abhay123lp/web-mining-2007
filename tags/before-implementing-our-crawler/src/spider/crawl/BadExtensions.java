package spider.crawl;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * A list of bad extensions that need not be kept in the frontier
 *
 * @author Gautam Pant
 */
public class BadExtensions {
    String[] ext = {"gif", "jpg", "jpeg", "mpg", "mpeg", "avi", "bmp", "pdf", "ps", "pdf", "exe", "doc", "ppt", "gz", "zip", "tar", "tgz", "z", "xml"};

    Hashtable fastLookup = new Hashtable();

    public BadExtensions() {
        load(ext);
    }

    public BadExtensions(String[] list) {
        if (list == null) {
            load(ext);
        } else {
            load(list);
        }
    }

    /**
     * find out if the given URL has a bad extension
     */
    public boolean hasBadExtension(String url) {
        //if the url contains cgi-bin directory than it must be ignored
        if (Pattern.matches(".+/cgi-bin.*", url)) {
            return true;
        }
        //break the url into tokens seperated by /
        String[] parts = Pattern.compile("//").split(url);
        //last token may be the filename
        String filename = parts[parts.length - 1];
        if (Pattern.matches(".+\\..+", filename)) {
            String[] fileParts = Pattern.compile("\\.").split(url);
            if (fastLookup.containsKey(fileParts[fileParts.length - 1].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void load(String[] extensions) {
        for (int i = 0; i < extensions.length; i++) {
            fastLookup.put(extensions[i], "");
        }
    }

}
