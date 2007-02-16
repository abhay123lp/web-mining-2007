package spider.crawl;

import spider.util.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A blog that builds context of a URL through DOM tree representation of an HTML page
 *
 * @author Gautam Pant
 * @see BasicCrawler
 */
public class DOMCrawler extends BasicCrawler {

    protected String query = null; //the query that biases the search
    protected boolean stemmer = true;
    protected double alpha = 0.25; //relative importance of page vs context

    protected int delta = -1; //the relative depth at which to set the aggregation node
    //if negative the parent is used as the aggregation node
    protected Stopper stp = new Stopper("stop_words");
    ;

    public DOMCrawler(String[] seeds, long maxPages, String dir) {
        super(seeds, maxPages, dir);
    }

    public DOMCrawler(String[] seeds, long maxPages, String dir, int delta) {
        super(seeds, maxPages, dir);
        this.delta = delta;
    }

    /**
     * @param Hashtable - link context, double - score of the parent page
     * @return Hashtable - with scores as values for each url
     */
    public Hashtable getURLScores(Hashtable lc, double pageScore, String parentURL) {
        if (query == null) {
            System.err.println("No query provided to bias the search");
            System.exit(1);
        }
        Hashtable ht = new Hashtable();
        for (Enumeration e = lc.keys(); e.hasMoreElements();) {
            String url = (String) e.nextElement();
            String context = (String) lc.get(url);
            //stop the context
            context = stp.stopString(context);
            //stem the context and query if so needed
            if (stemmer) {
                Stemmer st = new Stemmer();
                context = st.stem(context);
                st = new Stemmer();
                query = st.stem(query);
            }
            double score = pageScore * alpha + (1 - alpha) * Helper.getSim(query, context);
            ht.put(url, new Double(score));
        }
        return ht;
    }

    /**
     * overrides the base class method that gets the score for a page
     */
    protected double getPageScore(XMLParser p) {
        String text = p.getText();
        //stop the text
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
     * overrides the BasicCrawler's addToFrontier()
     */
    protected void addToFrontier(XMLParser p, String link) {
        if (p.startParser()) {
            Hashtable lc = null;
            if (delta < 0) {
                lc = p.getLinkContext(1);
            } else {
                //System.out.println("Delta"+delta);
                lc = p.getLinkContext(delta);
            }
            if (lc != null) {
                //if there were no links
                if (lc.size() == 0) {
                    return;
                }
                double pageScore = getPageScore(p);
                Hashtable scores = getURLScores(lc, pageScore, link);

                //dereference memory for optimizations
                lc = null;

                //convert the scores hashtable to a vector whose elements are comparable and then sort
                Vector urls = new Vector();
                for (Enumeration e = scores.keys(); e.hasMoreElements();) {
                    String url = (String) e.nextElement();
                    double urlScore = ((Double) scores.get(url)).doubleValue();

                    //check if the redirected url exists and if so replace url with it
                    String rurl = null;
                    if ((rurl = Redirections.getLocation(url)) != null) {
                        url = rurl;
                    }

                    //find out if the url is permissible according to RobotExclusion
                    String server = Helper.getHostNameWithPort(url);
                    Vector perm = robot.get(server);
                    if (perm != null) {
                        if (RobotExclusion.isDisallowed(url, perm)) {
                            continue;
                        }
                    }

                    if (!history.isInHistory(url)
                            && !bext.hasBadExtension(url) && !BadURLList.isBadURL(url) && (url != null)) {
                        urls.add(new FrontierElement(url, urlScore));
                    }
                }
                if (urls.size() == 0) {
                    return;
                }
                Collections.sort(urls);
                //just add the top N urls to the frontier
                /*Vector top = new Vector();
                    if (urls.size() > N ) {
                        for (int i = 0; i < N; i++) {
                            top.add(urls.get(i));
                        }
                    }
                    else {
                        top = urls;
                    }*/
                //long t1 = System.currentTimeMillis();
                //System.out.println("Adding:"+top.size());
                front.addElements(urls);
                //long total = System.currentTimeMillis() - t1;
                //System.out.println(total);
            }
        }
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
