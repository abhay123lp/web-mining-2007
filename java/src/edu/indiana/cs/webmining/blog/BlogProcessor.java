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
import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.bean.BlogInfo;
import edu.indiana.cs.webmining.blog.impl.BlogDBManager;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Feb 15, 2007
 * <p/>
 * This will be responsible for all the blog url handling activities
 */
public class BlogProcessor implements Runnable {

    private BlogDBManager dbManager;

    private File blogFileStore;

    public static long totatProcessedPageCount = 0;
    private BlogDetector blogDetector = BlogDetector.getInstance();

    private BlogCrawlingContext context;

    public BlogProcessor(BlogCrawlingContext context) throws BlogCrawlingException {
        this.context = context;
        this.blogFileStore = context.getFileStore();
        try {
            dbManager = BlogDBManager.getInstance();
        } catch (IOException e) {
            throw new BlogCrawlingException(e);
        }
    }

    public void run() {

        System.out.println("Starting Blog Processor ...");
        while (true) {
            try {
// fetch blog url to process
                BlogInfo blogInfo = dbManager.getNextBlogToProcess();

                // find the file and process it
                if (blogInfo != null) {

                    System.out.println("Starting to process blog [" + blogInfo.getUrl() + " ]");
                    File blogPage = new File(blogFileStore, blogInfo.getFileName());
                    try {
                        processPage(blogPage, blogInfo.getUrl());
                        System.out.println("Finished processing Blog [" + blogInfo.getUrl() + " ]");
                    } catch (BlogCrawlingException e) {
                        dbManager.setBlogProcessingFailed(blogInfo.getUrl());
                        System.out.println("Blog processing failed [" + blogInfo.getUrl() + " ]");

                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }

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
    private String[] processPage(File webPage, String pageURL) throws BlogCrawlingException {

        // let's not fetch media files
        if (blogDetector.isMediaFile(pageURL)) return new String[0];

        try {

            // first let's get the blog id. If this URL is not a blog, this should return
            // Constants.NOT_A_BLOG
//            int blogId = blogDetector.identifyURL(pageURL, webPage);

//            if (blogId > 0) {             // if this is a processable blog

            // process it and get the grouped set of urls. The map returned will contain urls as the key
            // and url type as the value.
            FileInputStream fileInputStream = new FileInputStream(webPage);
            String[] result = processBlog(fileInputStream);

            // save the link connection information.
            dbManager.insertBlogLinks(pageURL, result);
            fileInputStream.close();

            // return the the set of urls to be fetched for further processing
            return result;

//            }

        } catch (FileNotFoundException e) {
            dbManager.setBlogProcessingFailed(pageURL);
            throw new BlogCrawlingException(e);
        } catch (IOException e) {
            dbManager.setBlogProcessingFailed(pageURL);
            throw new BlogCrawlingException(e);
        } catch (SQLException e) {
            dbManager.setBlogProcessingFailed(pageURL);
            throw new BlogCrawlingException(e);
        }

    }

    private String[] processBlog(InputStream in) throws BlogCrawlingException {

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
}
