package spider.crawl;

import spider.util.Helper;
import spider.util.Stemmer;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Gautam Pant
 */
public class HubSeeker extends DOMCrawler {

    /**
     * domains of known URLs
     */
    private Hashtable knownDomain = null;

    public HubSeeker(String[] seeds, long maxPages, String dir) {
        super(seeds, maxPages, dir);
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
        Hashtable domains = new Hashtable(); //seed domains in URLs
        for (Enumeration e = lc.keys(); e.hasMoreElements();) {
            String url = (String) e.nextElement();
            String context = (String) lc.get(url);

            //get the domain of url
            String domain = Helper.getHostName(url);
            if (domain != null) {
                //if there are no knownDomain then consider seed domains as known domains
                if (seedDomain.containsKey(domain)) {
                    domains.put(domain, "");
                    /*if (domains.size() > 1) {
                             System.out.println("SeedDomains: "+domains);
                         }*/
                }
            }

            //stop context
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

        //number of unique seed domains
        double n = (double) domains.size();
        //find the hub_score
        double hubScore = n * (n - 1) / (1 + n * n);

        if (n > 1) {
            System.out.println("HubScore for parentURL:" + parentURL + " " + hubScore);
        }

        //find the max(score, hubscore) and use that as the score for the URL
        for (Enumeration e = ht.keys(); e.hasMoreElements();) {
            String url = (String) e.nextElement();
            double score = ((Double) ht.get(url)).doubleValue();
            //System.out.println(url+" "+score);
            double max = Math.max(hubScore, score);
            //System.out.println(url+" "+max);
            ht.put(url, new Double(max));
        }
        return ht;
    }

    /*public void setKnownDomain(Hashtable known) {
         if (known == null) {
             System.err.println("No Known URLs found");
             System.exit(1);
         }
         knownDomain = known;
     }*/
}
