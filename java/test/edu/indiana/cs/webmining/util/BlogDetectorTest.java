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

package edu.indiana.cs.webmining.util;

import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.blog.BlogDetector;
import junit.framework.TestCase;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Eran Chinthaka (echintha@cs.indiana.edu)
 * Date: Feb 1, 2007
 */
public class BlogDetectorTest extends TestCase {

    private String blogs = "test-resources/blogs.txt";
    private String nonBlogs = "test-resources/non-blogs.txt";

    public void testBlogDetection() {
        BlogDetector blogDetector = new BlogDetector(null);

        try {
            System.out.println("---- Checking Blog Sites ----");
            // first let's see how our tool responds to good urls
            BufferedReader in = new BufferedReader(new FileReader(blogs));
            String str;
            while ((str = in.readLine()) != null) {
                if (!str.startsWith("#") && !"".equals(str.trim())) {  // alowing for lines to be commented out.
                    System.out.println("site = " + str);
                    assertTrue(blogDetector.identifyURL(str, null) != -1);
                }
            }
            in.close();

            System.out.println("\n\n---- Checking Non-Blog Sites ----");

            // next non-blogs
            in = new BufferedReader(new FileReader(nonBlogs));
            while ((str = in.readLine()) != null) {
                if (!str.startsWith("#")) {  // alowing for lines to be commented out.
                    System.out.println("site = " + str);
                    assertTrue(blogDetector.identifyURL(str, null) == -1);
                }
            }
            in.close();

        } catch (IOException e) {
            System.out.println("Too bad. We've got an IOException" + e);
        }
    }

    public void testBlogRollingTop500Blogs() throws IOException, ParserException {

        Map<String, Boolean> linksToBeAvoided = new HashMap<String, Boolean>();
        linksToBeAvoided.put("http://drudgereport.com", Boolean.TRUE);
        linksToBeAvoided.put("http://zeldman.com", Boolean.TRUE);
        linksToBeAvoided.put("http://truthlaidbear.com", Boolean.TRUE);
        linksToBeAvoided.put("http://tenth-muse.com", Boolean.TRUE);
        linksToBeAvoided.put("http://janegalt.net", Boolean.TRUE);    // can not connect using crawler
        linksToBeAvoided.put("http://parastood.com", Boolean.TRUE);   // url redirection in a weird way
        linksToBeAvoided.put("http://khabgard.com", Boolean.TRUE);     // character set encoding problem
        linksToBeAvoided.put("http://denbeste.nu", Boolean.TRUE);     // URL redirection

        try {
            URL blogRollingTop500Page = new URL("http://www.blogrolling.com/top.phtml");
            Page page = new Page(blogRollingTop500Page.openConnection());
            Parser parser = new Parser(new Lexer(page));

            BlogDetector detector = new BlogDetector(null);
            TagNameFilter linkTag = new TagNameFilter("a");

            NodeList nl = parser.parse(linkTag);

            for (int i = 0; i < nl.size(); i++) {
                Node node = nl.elementAt(i);
                LinkTag linkTagNode = (LinkTag) node;

                // I don't wanna analyze all the links here. When  I was looking at the top 500 page
                // all the links that were linking to blogs, had target="_blank"
                if ("_blank".equals(linkTagNode.getAttribute("target")) && linkTagNode.getAttribute("title") != null) {
                    String pageURL = linkTagNode.getLink();
                    System.out.println("Link = " + pageURL);
                    if (!linksToBeAvoided.containsKey(pageURL)) {
                        assertTrue(detector.identifyURL(pageURL, null) == Constants.BLOG);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();

        }


    }

}
