package spider.crawl;

/**
 * @author Gautam Pant
 *         <p/>
 *         Defines an element in the frontier (unvisited URLs list)
 */
public class FrontierElement implements Comparable {
    /**
     * unvisted URL
     */
    public String url = null;

    /**
     * potential score of the URL
     */
    public double score = 0;

    /**
     * known depth of the URL from a starting page
     */
    public int depth = 0;

    /**
     * context of the URL - a few words that seem to describe the URL
     */
    public String context = null;

    /**
     * status of the URL
     */
    public int status = 0;

    public FrontierElement(String url) {
        this.url = url;
    }

    public FrontierElement(String url, double score) {
        this.url = url;
        this.score = score;
    }

    public FrontierElement(String url, double score, int depth) {
        this.url = url;
        this.score = score;
        this.depth = depth;
    }

    public FrontierElement(String url, double score, int depth, String context) {
        this.url = url;
        this.score = score;
        this.depth = depth;
        this.context = context;
    }

    public int compareTo(Object o) {
        FrontierElement he = (FrontierElement) o;
        if (this.score < he.score) {
            return 1;
        } else if (this.score == he.score) {
            return 0;
		}
		return -1;			
	} 
	
}