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

import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.blog.impl.DBBasedBlogDataStorage;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import spider.crawl.BasicCrawler;
import spider.crawl.Globals;
import spider.util.Redirections;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Feb 15, 2007
 * <p/>
 * This will be responsible for all the blog url handling activities
 */
public class BlogProcessingSystem {

    private BlogDataStorage blogDataStorage;

    public static final String SYSTEM_NAME = "BlogProcessingSystem";
    Logger logger = Logger.getLogger(SYSTEM_NAME);
    public static final String BLOG_DETECTION_PROPERTIES = "etc/blog-detection.properties";


    public static long totatProcessedPageCount = 0;
    private BlogDetector blogDetector = BlogDetector.getInstance();

    public BlogProcessingSystem() {

        try {

            Properties props = new Properties();
            props.load(new FileInputStream(BLOG_DETECTION_PROPERTIES));

            blogDataStorage = new DBBasedBlogDataStorage();
//            blogDataStorage = new FileBasedBlogDataStorage(props.getProperty("blog-data-folder"));

            FileHandler fileHandler = new FileHandler(props.getProperty("log-file"), true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This will process/save a given web page. First this needs to identify whether the given url
     * is a blog or not. If it is a blog, then this should handover the processing of the page
     * to the relevant BlogProcessor which will return a set of URLs to be fetched by the crawler.
     * <p/>
     * Having got the return from the Blog processor, this method should then save all the relevant
     * information, like link information, the whole page, etc,
     *
     * @param webPage - the location where the web site is saved.
     * @param pageURL - original url of the web page
     * @return list of urls to be fetched.
     * @throws BlogCrawlingException
     */
    public String[] processPage(File webPage, String pageURL) throws BlogCrawlingException {

        // let's not fetch media files
        if (blogDetector.isMediaFile(pageURL)) return new String[0];


        totatProcessedPageCount++;
        try {

            // first let's get the blog id. If this URL is not a blog, this should return
            // Constants.NOT_A_BLOG
            int blogId = blogDetector.identifyURL(pageURL, new FileInputStream(webPage));

            if (blogId > 0) {             // if this is a processable blog

                // process it and get the grouped set of urls. The map returned will contain urls as the key
                // and url type as the value.
                String[] result = processBlog(pageURL, new FileInputStream(webPage));

                // save the link connection information.
                blogDataStorage.store(result, pageURL);

                // return the the set of urls to be fetched for further processing
                return result;

            }

        } catch (FileNotFoundException e) {
            throw new BlogCrawlingException(e);
        } catch (IOException e) {
            throw new BlogCrawlingException(e);
        }

        return new String[]{};
    }

    public String[] processBlog(String blogURL, InputStream in) throws BlogCrawlingException {

        // using a set here to avoid duplicates
        Set<String> linksToBlogs = new TreeSet<String>();

        try {

            Page page = new Page(in, null);
            Parser parser = new Parser(new Lexer(page));

            // register a filter to extract all the anchor tags
            TagNameFilter anchorTagsFilter = new TagNameFilter("a");

            StringBuffer buf = new StringBuffer();
            NodeList anchorTagsList = parser.parse(anchorTagsFilter);


            for (int i = 0; i < anchorTagsList.size(); i++) {
                Node node = anchorTagsList.elementAt(i);
                LinkTag tag = (LinkTag) node;
                String linkURL = tag.getLink();

                if (!blogDetector.isMediaFile(linkURL) && blogDetector.identifyURL(linkURL, null) != Constants.NOT_A_BLOG) {
                    // logger.info(" *BLOG Detected* ==> " + linkURL);
//                    System.out.println("*BLOG Detected* ==> " + linkURL);
                    linksToBlogs.add(linkURL);
                }
            }

            String[] links = new String[linksToBlogs.size()];
            int count = 0;
            Iterator<String> iterator = linksToBlogs.iterator();
            for (String linksToBlog : linksToBlogs) {
                links[count++] = linksToBlog;
            }

            return links;

        } catch (ParserException e) {
            throw new BlogCrawlingException(e);
        } catch (UnsupportedEncodingException e) {
            throw new BlogCrawlingException(e);
        } catch (IOException e) {
            throw new BlogCrawlingException(e);
        }
    }


    /**
     * This will save blog links information in to the database.
     *
     * @param result
     * @param sourceURL
     */
    private void saveLinkInformation(String[] result, String sourceURL) {
        for (int i = 0; i < result.length; i++) {
            String link = result[i];
            System.out.println("link = " + link);

        }
    }

    public static void main(String[] args) {

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(BLOG_DETECTION_PROPERTIES)));

            String seedUrls = props.getProperty("seed-urls");
            //a list of seeds
            String[] urls = seedUrls.split(",");

            //number of pages to crawl
            int maxPages = Integer.parseInt(props.getProperty("max-pages"));

            //Folder to create to store the cache files (downloaded pages)
            String data = props.getProperty("data-folder");

            long startTime = System.currentTimeMillis();

            BasicCrawler bf = new BasicCrawler(urls, maxPages, data);

            //simultaneous threads of crawlers
            bf.setMaxThreads(Integer.parseInt(props.getProperty("max-threads")));

            //set maximum frontier size (-1 for no limit)
            bf.setMaxFrontier(Integer.parseInt(props.getProperty("frontier-size")));

            //history of pages crawled
            bf.setStorageFile(props.getProperty("crawl-history"));

            //log of a few statistics - file updated every minute
            bf.setStatFile(props.getProperty("statitics-file"));

            //set the e-mail address to go with the http request
            String email;
            Globals.setMail(props.getProperty("email"));

            bf.startCrawl();

            long endTime = System.currentTimeMillis();
            long total = endTime - startTime;
            System.out.println("Total Time: " + total);

            //info on redirected pages
            Redirections.toFile(props.getProperty("redirection-log"));
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}
