package spider.util;

/**
 * Class defines the web page data structure
 *
 * @author Gautam Pant
 */
public class Page {
    public String content = null;  //HTML content
    public long lastModified = 0; //in seconds
    public long size = 0; //in KB
    /**
     * code indicates potential problems with the page or some other event associetd with it
     * 0 - no problem
     * 1 - page not found
     * 2 - redirected page
     * 3 - time-out
     * 4 - other
     *
     * @see Fetcher
     */
    public int code = 4;
}
