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

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Feb 15, 2007
 * <p/>
 * This will be responsible for all the blog url handling activities
 */
public class BlogProcessingSystem {

    /**
     * This will process a given blog page, using a blog specific processor and will return set of URLs to be fetched.
     * Those urls must be fetched by a crawler.
     *
     * @param webPage
     * @param pageURL
     */
//    public void processPage(File webPage, String pageURL) {
//
//        BlogDetector.getInstance().isBlog(pageURL)
//
//        {
//        if (p.startParser()) {
//            String[] newLinks = p.getLinks();
//            if (newLinks != null) {
//                double pageScore = getPageScore(p);
//                String fileName = Hashing.getHashValue(url);
//                history.add(url, fileName, pageScore);
//                //System.out.println(url+" "+pageScore+" "+Hashing.getHashValue(url));
//                Vector urls = new Vector();
//                for (int j = 0; j < newLinks.length; j++) {
//
//                    //check if the redirected url exists and if so replace url with it
//                    /*String rurl = null;
//                    if ((rurl = Redirections.getLocation(newLinks[j]))
//                        != null) {
//                        newLinks[j] = rurl;
//                    }*/
//
//                    //find if the url violates known robot exclusion listings
//                    String server = Helper.getHostNameWithPort(newLinks[j]);
//                    Vector perm = robot.get(server);
//                    if (perm != null) {
//                        if (RobotExclusion.isDisallowed(newLinks[j], perm)) {
//                            continue;
//                        }
//                    }
//
//                    //add to frontier if not in history and not has bad extension
//                    if (!history.isInHistory(newLinks[j])
//                            && !bext.hasBadExtension(newLinks[j])
//                            && (newLinks[j] != null)) {
//                        urls.add(new FrontierElement(newLinks[j], pageScore));
//                    }
//                }
//                if (urls.size() == 0) {
//                    return;
//                }
//                front.addElements(urls);
//            }
//        } else {
//            stat.parseErrors(1);
//        }
//    }
//    }

}
