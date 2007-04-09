package spider.crawl;

import spider.util.Redirections;

/**
 * Example code for running a blog
 * make sure you put a valid e-mail address
 *
 * @author Gautam Pant
 */
public class Tester {

    //PLEASE PUT YOU E-MAIL FOR HTTP REQUESTS
    public static String email = "echintha@cs.indiana.edu";

    public static void main(String[] args) {
        //a list of seeds
        String[] urls = new String[2];
        urls[0] = "http://hot.blogrolling.com/";
        urls[1] = "http://www.bloglines.com/blog/chinthaka";

        //number of pages to crawl
        int maxPages = 100;

        //Folder to create to store the cache files (downloaded pages)
        String data = "Data";

        long startTime = System.currentTimeMillis();

        BasicCrawler bf = new BasicCrawler(urls, maxPages, data);

        //simultaneous threads of crawlers
        bf.setMaxThreads(1);

        //set maximum frontier size (-1 for no limit)
        bf.setMaxFrontier(-1);

        //if a best first blog set a query
        //bf.setQuery("University Iowa");

        //history of pages crawled
        bf.setStorageFile("history.txt");

        //log of a few statistics - file updated every minute
        bf.setStatFile("statistics.txt");

        //set the e-mail address to go with the http request
        Globals.setMail(email);

        bf.startCrawl();

        long endTime = System.currentTimeMillis();
        long total = endTime - startTime;
        System.out.println("Total Time: " + total);

        //info on redirected pages
        Redirections.toFile("redirect.txt");
    }

}
