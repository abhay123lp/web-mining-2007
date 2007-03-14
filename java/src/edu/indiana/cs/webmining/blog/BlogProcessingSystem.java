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

import edu.indiana.cs.webmining.blog.impl.FileBasedBlogDataStorage;
import edu.indiana.cs.webmining.blog.impl.GenericBlogProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public BlogProcessingSystem() {
        blogDataStorage = new FileBasedBlogDataStorage();
        try {
            logger.addHandler(new FileHandler("logs/blog-processing.log", true));
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);
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
        List urlList = new ArrayList();
        try {

            // first let's get the blog id. If this URL is not a blog, this should return
            // Constants.NOT_A_BLOG
            int blogId = BlogDetector.getInstance().identifyURL(pageURL, new FileInputStream(webPage));

            if (blogId > 0) {             // if this is a processable blog
                // get the customized blog processor
                BlogProcessor blogProcessor = new GenericBlogProcessor();

                // process it and get the grouped set of urls. The map returned will contain urls as the key
                // and url type as the value.
                String[] result = blogProcessor.processBlog(pageURL, new FileInputStream(webPage));

                // save the link connection information.
                blogDataStorage.store(result, pageURL);

                // return the the set of urls to be fetched for further processing
                return result;

            }

        } catch (FileNotFoundException e) {
            throw new BlogCrawlingException(e);
        }

        return new String[]{};
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


}
