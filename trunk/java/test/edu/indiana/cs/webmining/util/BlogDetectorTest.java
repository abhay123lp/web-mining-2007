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

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.indiana.cs.webmining.blog.BlogDetector;

/**
 * User: Eran Chinthaka (echintha@cs.indiana.edu)
 * Date: Feb 1, 2007
 */
public class BlogDetectorTest extends TestCase {

    private String blogs = "test-resources/blogs.txt";
    private String nonBlogs = "test-resources/non-blogs.txt";

    public void testBlogDetection() {
        BlogDetector blogDetector = BlogDetector.getInstance();

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

}
