package spider.crawl;

import spider.util.Fetcher;
import spider.util.HTMLParser;
import spider.util.Hashing;
import spider.util.Helper;
import spider.util.Page;
import spider.util.Redirections;
import spider.util.RobotExclusion;
import spider.util.ThreadTimer;

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pool of multi-threaded fetchers that can be used to fetch many pages at the same time
 *
 * @author Gautam Pant
 */
public class FetcherPool {

    /**
     * maximum number of fetcher threads that may exist at any given time
     */
    private int max_fetchers = 0;

    /**
     * robot exclusions
     */
    private RobotExclusion robotsX = new RobotExclusion();

    /**
     * A pool of threads
     */
    private Vector pool = new Vector();

    /**
     * Cache used for storage
     */
    private Cache c = null;

    /**
     * locate and remove these tags from the content
     */
    public String[] tags = {"style", "script", "applet"};

    private int TIMEOUT = 40;
    private Statistics stat = null;

    public FetcherPool(
            int max_fetchers,
            Cache c,
            RobotExclusion robotsX,
            Statistics stat) {
        this.max_fetchers = max_fetchers;
        this.c = c;
        this.robotsX = robotsX;
        this.stat = stat;
    }

    //create upto 10 fetchers by default
    public FetcherPool(Cache c, RobotExclusion robotsX, Statistics stat) {
        max_fetchers = 10;
        this.c = c;
        this.robotsX = robotsX;
        this.stat = stat;
    }

    private Thread makeFetcherThread(final String url) {
        if (pool.size() < max_fetchers) {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        String filename = Hashing.getHashValue(url);
                        //fetch the page from the Web only if it does not exists in the local cache
                        File file = new File(c.getPath(filename) + filename);
                        if (file.exists()) {
                            stat.foundInCache(1);
                            return;
                        }
                        Fetcher f = new Fetcher();
                        //before fetching a page check for robot exclusion
                        String hostName = Helper.getHostNameWithPort(url);
                        //get disallowed paths
                        Vector perm = robotsX.get(hostName);
                        if (perm == null) {
                            //fetch robot exclusion
                            Page rp =
                                    f.fetch("http://" + hostName + "/robots.txt", Globals.eMail);
                            //if the file is not fetched or has no content the put an empty permissions vector
                            if (rp.content == null) {
                                Vector blank = new Vector();
                                robotsX.add(hostName, blank);
                            } else {
                                //else put the new permissions
                                Vector dis =
                                        RobotExclusion.getVector(rp.content);
                                robotsX.add(hostName, dis);
                            }
                            perm = robotsX.get(hostName);
                        }

                        //perm must not be null
                        if (perm == null) {
                            System.err.println(
                                    "Fatal Error: Robot Exclusion - disallowed paths could not be retrieved");
                            System.exit(1);
                        }

                        //check if the URL is okay to fetch based on robot Exclusion
                        if (RobotExclusion.isDisallowed(url, perm)) {
                            //System.out.println(url+" disallowed");
                            return; //exit from thread
                        }

                        //fetch the page
                        //System.out.println("Fetching:"+url);
                        Page p = f.fetch(url, Globals.eMail);
                        //update some statistics
                        if (p.code == 0) {
                            stat.addOkay(1);
                        } else if (p.code == 3) {
                            stat.addTimeouts(1);
                        } else if (p.code == 1) {
                            stat.notFound(1);
                        }

                        //url of the page fecthed (can be different from page asked for)
                        String pageURL = url;
                        //if there is a redirection for the URL change the pageURL to that
                        String newURL = null;
                        if ((newURL = Redirections.getLocation(url)) != null) {
                            pageURL = newURL;
                        }
                        //System.out.println("Fetched:"+url);
                        if (p != null) {
                            if (p.content != null) {
                                //html parsing may take too long if there are too many tags
                                HTMLParser hp = new HTMLParser();
                                //remove certains tags from the content
                                for (int t = 0; t < tags.length; t++) {
                                    Pattern ptn =
                                            Pattern.compile(
                                                    "<\\s*?"
                                                            + tags[t]
                                                            + ".*?<\\s*?/"
                                                            + tags[t]
                                                            + "\\s*?>",
                                                    Pattern.DOTALL
                                                            | Pattern.CASE_INSENSITIVE);
                                    Matcher m = ptn.matcher(p.content);
                                    if (m.find()) {
                                        p.content = m.replaceAll("");
                                    }
                                }
                                String out = hp.htmlToXML(p.content, pageURL);
                                c.addToCache(
                                        pageURL,
                                        filename,
                                        out,
                                        p.lastModified);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("*Error while fetching and parsing");
                        e.printStackTrace();
                    }
                }
            };
            return new Thread(r);
        }
        return null;
    }

    /**
     * called to fetch one or more pages
     */
    public void fetchPages(String[] urls) {
        String[] filenames = new String[urls.length];
        Thread[] fp = new Thread[urls.length];

        //if there is only one page to fetch we do not need to create threads
        //optimization code
        /*if (urls.length == 1){
              //System.out.println("Fetching"+ urls[0]);
              filenames[0] = Long.toString(Math.round(Math.random()*1e15));
              Fetcher f = new Fetcher();
              //System.out.println("Fetching: "+url);
              //fetch the page
              String url = urls[0];
              String filename = filenames[0];
              Page p = f.fetch(url);
              if ( p != null) {
                  if (p.content != null) {
                      HTMLParser hp = new HTMLParser();
                      String out = hp.htmlToXML(p.content, url);
                      c.addToCache(url, filename, out, p.lastModified);
                  }
              }
              return filenames;
          }*/

        //pages to be fetched
        for (int i = 0; i < urls.length; i++) {
            fp[i] = makeFetcherThread(urls[i]);
            fp[i].start();
        }
        ThreadTimer[] tt = new ThreadTimer[fp.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                //tt[i] = new ThreadTimer(fp[i], TIMEOUT*1000);
                //tt[i].start();
                //tt[i].join();
                fp[i].join();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
