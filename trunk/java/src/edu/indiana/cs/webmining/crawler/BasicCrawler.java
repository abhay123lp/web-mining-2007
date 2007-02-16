/* Copyright (C) 2004 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */

package edu.indiana.cs.webmining.crawler;

import spider.crawl.*;
import spider.util.Hashing;
import spider.util.Helper;
import spider.util.RobotExclusion;
import spider.util.XMLParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Feb 15, 2007
 * <p/>
 * Initial code extract from the works of Gautam Pant
 */
public class BasicCrawler {

    private String[] seeds = null;
    protected Hashtable seedDomain = new Hashtable();
    private long maxPages = 0; //maximum number of pages to fetch
    private int maxFrontier = 10000;
    //maximum size of the frontier (-1 for infinite)
    protected History history = new History();
    //hash table that stores visisted urls along with timestamp
    private String dir = null;
    private int maxThreads = 100; //maximum number of threads of the crawlers
    protected Cache cache = null;
    protected Frontier front = null;
    private int topN = 1;
    private String storageFile = "default"; //history storing file
    ActiveThreads activeThreads = new ActiveThreads();
    protected BadExtensions bext = new BadExtensions();
    protected RobotExclusion robot = new RobotExclusion();
    //cache of robots permissions for servers based on robots.txt file
    protected Statistics stat = null;
    private String statFile = "statistics.txt";
    private boolean frontierAdd = true;
    //allow or disallow addition to frontier

    /**
     * construct the crawler with the seeds
     *
     * @param seeds    - URLs that are starting points for crawl
     *                 maxPages - maximum pages to be fetched
     *                 dir - the directory to store the results in (the directory is created if it does not exist)
     * @param maxPages
     * @param dir
     */
    public BasicCrawler(String[] seeds, long maxPages, String dir) {
        this.seeds = seeds;
        this.maxPages = maxPages;
        this.dir = dir;
    }

    /**
     * create a thread of crawler
     *
     * @return - crawler thread
     */
    private Thread makeCrawlerThread() {
        Thread t = null;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    //add to thread count
                    activeThreads.add();
                    //while the required number of pages have not been crawled
                    //and frontier is non-empty
                    while (history.size() < maxPages) {
                        // System.out.println("Frontier:"+ front.size());
                        //select pages to crawl (pick top 10)
                        //if(front.toString().length() > 0) {
                        //	System.out.println(front.toString());
                        //}
                        Vector links = new Vector();
                        for (int i = 0; i < topN; i++) {
                            FrontierElement fe =
                                    (FrontierElement) front.getElement();
                            //if nothing was returned then frontier must be empty
                            if (fe != null) {
                                //System.out.println("Score:"+fe.score);
                                // make sure that a page from history is not refetched
                                if (!history.isInHistory(fe.url)) {
                                    links.add(fe.url);
                                }
                            } else {
                                break;
                            }
                        }
                        //if you got nothing from frontier sleep for 100ms
                        //and subtract from the thread count
                        if (links.size() == 0 && front.size() == 0) {
                            activeThreads.subtract();
                            while (front.size() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    continue;
                                }
                                //if all the threads seem to be waiting
                                if (activeThreads.get() == 0) {
                                    //activeThreads.add();
                                    //end the thread loop
                                    return;
                                } else {
                                    //check if there is anything in the frontier
                                    continue;
                                }
                            }
                            activeThreads.add();
                        }

                        String[] urls = new String[links.size()];
                        for (int i = 0; i < links.size(); i++) {
                            urls[i] = (String) links.get(i);
                        }

                        //fetch pages and store it in a cache
                        FetcherPool fp = new FetcherPool(cache, robot, stat);
                        //System.out.println("Fetch Pages");
                        fp.fetchPages(urls);
                        //System.out.println("Fetched Pages");

                        //extract links and add them to the frontier
                        for (int i = 0; i < urls.length; i++) {
                            //find the filename
                            String fileName = Hashing.getHashValue(urls[i]);
                            //check in the cache
                            File f = new File(cache.getPath(fileName) + fileName);
                            if (f.exists()) {
                                //score and add the URLs to frontier (if frontierAdd flag is true)
                                if (frontierAdd) {
                                    XMLParser p = new XMLParser(f);
                                    addToFrontier(p, urls[i]);
                                } else {
                                    history.add(urls[i], fileName, -1);
                                }
                            }
                        }

                    }
                    activeThreads.subtract();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t = new Thread(r);
        return t;
    }

    public boolean startCrawl() {

        //if parameters provided are not proper than do no start crawl
        if (dir == null || seeds == null || maxPages == 0) {
            return false;
        }
        System.out.println("starting crawler...");
        //check if the directory exists
        boolean exists = (new File(dir)).exists();
        //if not exist then create one with that name
        if (!exists) {
            // Create a directory; all non-existent ancestor directories are
            // automatically created
            boolean success = (new File(dir)).mkdirs();
            if (!success) {
                // Directory creation failed then do not crawl;
                System.err.println(
                        "Directory:" + dir + " could not be created");
                return false;
            }
        }

        //create cache
        cache = new Cache();
        //set directory
        cache.setPath(dir);

        //create a Frontier
        front = new Frontier(maxFrontier, cache);

        //add the seed URLs to the frontier
        Vector urls = new Vector();
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = Helper.getCanonical(seeds[i]);
            if (seeds[i] == null) {
                continue;
            }
            urls.add(new FrontierElement(seeds[i], 1));
            //get the top level domain of seed url and store it in hash
            String d = Helper.getHostName(seeds[i]);
            if (d != null) {
                seedDomain.put(d, "");
            }
            //fes.add(new FrontierElement(seeds[i], 1));
        }
        front.addElements(urls);
        System.out.println("Frontier:" + front.size());

        stat = new Statistics(System.currentTimeMillis(), history, front);
        stat.setFile(statFile);
        //start the threads
        Thread[] crawlers = new Thread[maxThreads];
        for (int i = 0; i < maxThreads; i++) {
            //System.out.println("Starting thread " +i);
            crawlers[i] = makeCrawlerThread();
            crawlers[i].start();
        }
        //start statistics
        stat.start();
        //join and wait for the threads to get over
        for (int i = 0; i < maxThreads; i++) {
            try {
                crawlers[i].join();
            } catch (Exception e) {
                continue;
            }
        }

        //make sure that all the threads are dereferenced
        for (int i = 0; i < maxThreads; i++) {
            try {
                crawlers[i] = null;
            } catch (Exception e) {
                continue;
            }
        }
        stat.setStop(true);
        stat.toFile();
        //System.out.println("History:"+history.size());
        //save the history
        history.toFile(storageFile);
        return true;
    }

    /**
     * score and add URls to the frontier
     * more sophiticated code may extend or override the functionality
     *
     * @param p - the XML parser, id - the filename of the page being parsed
     */
    protected void addToFrontier(XMLParser p, String url) {
        if (p.startParser()) {
            String[] newLinks = p.getLinks();
            if (newLinks != null) {
                double pageScore = getPageScore(p);
                String fileName = Hashing.getHashValue(url);
                history.add(url, fileName, pageScore);
                //System.out.println(url+" "+pageScore+" "+Hashing.getHashValue(url));
                Vector urls = new Vector();
                for (int j = 0; j < newLinks.length; j++) {

                    //check if the redirected url exists and if so replace url with it
                    /*String rurl = null;
                    if ((rurl = Redirections.getLocation(newLinks[j]))
                        != null) {
                        newLinks[j] = rurl;
                    }*/

                    //find if the url violates known robot exclusion listings
                    String server = Helper.getHostNameWithPort(newLinks[j]);
                    Vector perm = robot.get(server);
                    if (perm != null) {
                        if (RobotExclusion.isDisallowed(newLinks[j], perm)) {
                            continue;
                        }
                    }

                    //add to frontier if not in history and not has bad extension
                    if (!history.isInHistory(newLinks[j])
                            && !bext.hasBadExtension(newLinks[j])
                            && (newLinks[j] != null)) {
                        urls.add(new FrontierElement(newLinks[j], pageScore));
                    }
                }
                if (urls.size() == 0) {
                    return;
                }
                front.addElements(urls);
            }
        } else {
            stat.parseErrors(1);
        }
    }

    /**
     * scores a page and returns the score
     * more sophisticated code may override the functionality
     */
    protected double getPageScore(XMLParser p) {
        return -1;
    }

    /**
     * Returns the maxFrontier.
     *
     * @return long
     */
    public long getMaxFrontier() {
        return maxFrontier;
    }

    /**
     * Sets the maxFrontier - maximum size of the frontier.
     *
     * @param maxFrontier The maxFrontier to set
     */
    public void setMaxFrontier(int maxFrontier) {
        this.maxFrontier = maxFrontier;
        System.out.println("Maximum Frontier set to: " + maxFrontier);
    }

    /**
     * Returns the maxThreads.
     *
     * @return int
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Sets the maxThreads - maximum number of threads.
     *
     * @param maxThreads The maxThreads to set
     */
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * Returns the maxPages.
     *
     * @return long
     */
    public long getMaxPages() {
        return maxPages;
    }

    /**
     * Returns the topN.
     *
     * @return int
     */
    public int getTopN() {
        return topN;
    }

    /**
     * Sets the topN.
     *
     * @param topN The topN to set
     */
    public void setTopN(int topN) {
        this.topN = topN;
    }

    /**
     * Returns the storageFile.
     *
     * @return String
     */
    public String getStorageFile() {
        return storageFile;
    }

    /**
     * Sets the storageFile.
     *
     * @param storageFile The storageFile to set
     */
    public void setStorageFile(String storageFile) {
        this.storageFile = storageFile;
    }

    /**
     * Sets the statFile.
     *
     * @param statFile The statFile to set
     */
    public void setStatFile(String statFile) {
        this.statFile = statFile;
    }

    /**
     * Allows to restart the crawler based on the last state of the history.
     * <p/>
     * loads history and fill up the corresponding frontier
     */
    public boolean reStartCrawl() {

        //if parameters provided are not proper than do no start crawl
        if (dir == null || seeds == null || maxPages == 0) {
            return false;
        }
        System.out.println("restarting crawler...");
        //check if the directory exists
        boolean exists = (new File(dir)).exists();
        //if not exist then create one with that name
        if (!exists) {
            // Create a directory; all non-existent ancestor directories are
            // automatically created
            boolean success = (new File(dir)).mkdirs();
            if (!success) {
                // Directory creation failed then do not crawl;
                System.err.println(
                        "Directory:" + dir + " could not be created");
                return false;
            }
        }

        //create cache
        cache = new Cache();
        //set directory
        cache.setPath(dir);

        //create a Frontier
        front = new Frontier(maxFrontier, cache);

        //set statistics file
        stat = new Statistics(System.currentTimeMillis(), history, front);
        stat.setFile(statFile);

        //load history and frontier based on the current history file
        System.out.println("loading history and frontier...");
        File oldHist = new File(storageFile);
        if (oldHist.exists()) {
            try {
                BufferedReader bf = new BufferedReader(new FileReader(oldHist));
                Hashtable files = new Hashtable();
                String line = null;
                while ((line = bf.readLine()) != null) {
                    String[] parts = line.split("\\s+");
                    history.add(
                            parts[1],
                            parts[2],
                            Double.parseDouble(parts[3]));
                    files.put(parts[1], parts[2]);

                }
                for (Enumeration e = files.keys(); e.hasMoreElements();) {
                    String url = (String) e.nextElement();
                    String filename = (String) files.get(url);
                    //System.out.println(cache.getPath(filename)+filename);
                    XMLParser p =
                            new XMLParser(new File(cache.getPath(filename) + filename));
                    addToFrontier(p, url);
                }
                bf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No history found");
            System.exit(1);
        }

        System.out.println("Frontier:" + front.size());

        //start the threads
        Thread[] crawlers = new Thread[maxThreads];
        for (int i = 0; i < maxThreads; i++) {
            //System.out.println("Starting thread " +i);
            crawlers[i] = makeCrawlerThread();
            crawlers[i].start();
        }
        //start statistics
        stat.start();
        //join and wait for the threads to get over
        for (int i = 0; i < maxThreads; i++) {
            try {
                crawlers[i].join();
            } catch (Exception e) {
                continue;
            }
        }

        //make sure that all the threads are dereferenced
        for (int i = 0; i < maxThreads; i++) {
            try {
                crawlers[i] = null;
            } catch (Exception e) {
                continue;
            }
        }
        stat.setStop(true);
        stat.toFile();
        //System.out.println("History:"+history.size());
        //save the history
        history.toFile(storageFile);
        return true;
    }

    /**
     * set the frontier to allow (true) or disallow (false) addition of new URLs.
     *
     * @param b
     */
    public void setFrontierAdd(boolean b) {
        frontierAdd = b;
    }


    
}
