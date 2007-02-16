package spider.crawl;

import spider.util.Helper;
import spider.util.Stemmer;
import spider.util.Stopper;
import spider.util.XMLParser;

/**
 * Best First crawler (that is extended from a BreadthFirst crawler)
 *
 * @author Gautam Pant
 */
public class BestFirst extends BasicCrawler {

    protected String query = null; //the query that biases the search
    protected boolean stemmer = true;
    protected Stopper stp = new Stopper("stop_words");

    public BestFirst(String[] seeds, long maxPages, String dir) {
        super(seeds, maxPages, dir);
    }


    /**
     * overrides the base class method that gets the score for a page
     */
    protected double getPageScore(XMLParser p) {
        String text = p.getText();
        //stop text
        text = stp.stopString(text);
        if (stemmer) {
            Stemmer st = new Stemmer();
            text = st.stem(text);
            st = new Stemmer();
            query = st.stem(query);
        }
        double score = Helper.getSim(query, text);
        return score;
    }

    /**
     * Returns the query.
     *
     * @return String
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query.
     *
     * @param query The query to set
     */
    public void setQuery(String query) {
        this.query = query.toLowerCase();
        query = stp.stopString(query);
	}

}
