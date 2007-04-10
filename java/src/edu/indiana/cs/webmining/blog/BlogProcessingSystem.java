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

package edu.indiana.cs.webmining.blog;

import edu.indiana.cs.webmining.BlogCrawlingContext;
import edu.indiana.cs.webmining.blog.impl.BlogDBManager;
import edu.indiana.cs.webmining.crawler.Crawler;
import edu.indiana.cs.webmining.db.ConnectionPool;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Apr 9, 2007
 */
public class BlogProcessingSystem {
    public static final String BLOG_DETECTION_PROPERTIES = "etc/blog-detection.properties";
    List<Thread> threadBucket = new ArrayList<Thread>();

    public void start() {

        // set http client logging off
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
                "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
                "error");

        try {
// load the blog processing context
            BlogCrawlingContext context = new BlogCrawlingContext(BLOG_DETECTION_PROPERTIES);

            // init connection pool
            initDBConnectionPool();

            // create proper number of crawler threads and start them
            startCrawlers(context);

            // create blog processing threads and start them
            startBlogProcessingThreads(context);

//            try {
//                Thread.sleep(1000 * 60 * 3);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//
//            }

            while (threadBucket.size() > 0) {
                for (int i = 0; i < threadBucket.size(); i++) {
                    if (!threadBucket.get(i).isAlive()) {
                        System.out.println("Removing thread " + i);
                        threadBucket.remove(i);
                    } else {
                        System.out.println("Living Thread => " + i);
                    }
                }
                try {
                    Thread.currentThread().sleep(1000 * 60 * 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }

            }
            System.out.println("Finished ..");
        } catch (BlogCrawlingException e) {
            e.printStackTrace();

        }
    }

    private void initDBConnectionPool() {
        try {
            Properties props = new Properties();
//        props.load(new FileInputStream("etc/sql-silo-echintha.prop"));
            props.load(new FileInputStream("etc/sql-local.prop"));
            ConnectionPool connectionPool = ConnectionPool.getInstance();
            connectionPool.init(props.getProperty("driverClassName"), props.getProperty("url"), props.getProperty("username"), props.getProperty("password"), 20, 50,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void startBlogProcessingThreads(BlogCrawlingContext context) throws BlogCrawlingException {
        int maxBlogProcessorThreadCount = context.getMaxBlogProcessorThreadCount();
        for (int i = 0; i < maxBlogProcessorThreadCount; i++) {
            Thread blogProcessingThread = new Thread(new BlogProcessor(context));
            threadBucket.add(blogProcessingThread);
            blogProcessingThread.setDaemon(false);
            blogProcessingThread.start();

        }
    }

    private void startCrawlers(BlogCrawlingContext context) throws BlogCrawlingException {
        try {
            // put the seed urls in to the database
            String[] seedUrls = context.getSeedUrls();
            new BlogDBManager().insertLinksToBeFetchedByTheCrawler(seedUrls);

            // start the given number of crawler threads
            int crawlThreadCount = context.getMaxCrawlThreadCount();
            for (int i = 0; i < crawlThreadCount; i++) {
                Thread crawlThread = new Thread(new Crawler(context));
                crawlThread.setDaemon(false);
                threadBucket.add(crawlThread);
                crawlThread.start();
            }
        } catch (BlogCrawlingException e) {
            e.printStackTrace();
            throw new BlogCrawlingException(e);
        }
    }

    public static void main(String[] args) {
        BlogProcessingSystem blogProcessingSystem = new BlogProcessingSystem();
        blogProcessingSystem.start();
    }


}
