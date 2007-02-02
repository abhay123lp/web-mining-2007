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
/**
 * User: Eran Chinthaka (echintha@cs.indiana.edu)
 * Date: Feb 1, 2007
 */
package edu.indiana.cs.webmining.util;

import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.Parser;
import org.htmlparser.Node;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.NodeList;
import org.htmlparser.filters.TagNameFilter;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

public class BlogDetector {
    private static BlogDetector ourInstance = new BlogDetector();

    private static List<String> knownBlogURLList;
    private static Map<String, Boolean> blogPublishingFrameworks;

    public static BlogDetector getInstance() {
        return ourInstance;
    }

    private BlogDetector() {
        intialize();
    }

    /**
     * Here we will be pre-populating hash tables with the known blogs and the checks related to them.
     * We made this singleton to avoid the costs of re-initializing these parameters, but if this seems
     * a bottleneck whilst crawlign, let's think about making this a generic class.
     */
    private void intialize() {
        knownBlogURLList = new ArrayList<String>();
        knownBlogURLList.add("blogspot.com");
        knownBlogURLList.add("blog.myspace.com");
        knownBlogURLList.add("blogger.com");
        knownBlogURLList.add("weblogs.com");
        knownBlogURLList.add("diaryland.com");
        knownBlogURLList.add("livejournal.com");
        knownBlogURLList.add("journalspace.com");
        knownBlogURLList.add("blogalia.com");
        knownBlogURLList.add("pitas.com");
        knownBlogURLList.add("persianblog.com");
        knownBlogURLList.add("bpersianlog.com");
        knownBlogURLList.add("diaryhub.com");
        knownBlogURLList.add("diaryhub.net");
        knownBlogURLList.add("radio.weblogs.com");
        knownBlogURLList.add("blogs.law.harvard.edu");
        knownBlogURLList.add("blogs.it");
        knownBlogURLList.add("manilasites.com");
        knownBlogURLList.add("editthispage.com");
        knownBlogURLList.add("weblogger.com");
        knownBlogURLList.add("typepad");
        knownBlogURLList.add("twoday.net");
        knownBlogURLList.add("blogs.salon.com");
        knownBlogURLList.add("blogs.salon.com");
        knownBlogURLList.add("jroller.com");
        knownBlogURLList.add("diarist.com");
        knownBlogURLList.add("antville.org");
        knownBlogURLList.add("bloggingnetwork.com");
        knownBlogURLList.add("crimsonblog.com");
        knownBlogURLList.add("skyblog.com");
        knownBlogURLList.add("blog.pl");
        knownBlogURLList.add("e-blog.pl");
        knownBlogURLList.add("weblog.pl");
        knownBlogURLList.add("monblogue.com");
        knownBlogURLList.add("joueb.com");
        knownBlogURLList.add("blogstudio.com");
        knownBlogURLList.add("blog-city.com");
        knownBlogURLList.add("blogsky.com");
        knownBlogURLList.add("u-blog.net");
        knownBlogURLList.add("bbarrapunto.com");
        knownBlogURLList.add("blig");
        knownBlogURLList.add("g-blog.net");
        knownBlogURLList.add("babelogue.citypages.com");
        knownBlogURLList.add("jevon.org");
        knownBlogURLList.add("tripod.com");
        knownBlogURLList.add("spaces.live.com");
        knownBlogURLList.add("1060.org/blogxter");

        blogPublishingFrameworks = new HashMap<String, Boolean>();
        blogPublishingFrameworks.put("http://www.sixapart.com/movabletype/", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.movabletype.org/", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.modblog.com", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.feedblitz.com/", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.lifetype.net/", Boolean.TRUE);
    }

    /**
     * This will detect whether a URL given  is pointed to a blog or not. There are striclty defined set of rules
     * to identify blogs as of now. But this indentification is based on http://search.cpan.org/src/MCEGLOWS/WWW-Blog-Identify-0.06/Identify.pm
     * <p/>
     * This algorithm said to be favouring false negatives over false positives.
     * <p/>
     * We might improve this algorithm as and when we find interesting points.
     *
     * @param pageURL
     * @return
     */
    public boolean isBlog(String pageURL) {
        System.out.println("pageURL = " + pageURL);
        URL url = null;
        try {

            // first let's avoid traps. .
            if (pageURL == null || "".equals(pageURL)) {
                return false;
            }

            return isBlog(new URL(pageURL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This will detect whether a URL given  is pointed to a blog or not. There are striclty defined set of rules
     * to identify blogs as of now. But this indentification is based on http://search.cpan.org/src/MCEGLOWS/WWW-Blog-Identify-0.06/Identify.pm
     * <p/>
     * This algorithm said to be favouring false negatives over false positives.
     * <p/>
     * We might improve this algorithm as and when we find interesting points.
     *
     * @param pageURL
     * @return
     */
    public boolean isBlog(URL pageURL) {
        // sorry, we do not handle anything other than http. Can there be smtp or tcp blogs?
        if (!"http".equals(pageURL.getProtocol())) {
            return false;
        }

        // First let's look at the blog address. If the word blog is in the host address or if this is one of the known
        // blogs then we are done, with the minimal effort
        String hostAddress = pageURL.getHost();
        if (hostAddress.contains("blog") || isKnownBlog(hostAddress)) {
            return true;
        }

        // now let's see whether the url contains, blog as a word
        if (pageURL.getFile().contains("blog")) {
            return true;
        }

        // hmm, now surface scans are over. Let's look at the page now.

        try {
// first let's see whether title has XX's blog in it
            Page page = new Page(pageURL.openConnection());
            Parser parser = new Parser(new Lexer(page));
            TagNameFilter titleFilter = new TagNameFilter("title");
            NodeList titles = parser.parse(titleFilter);

            if (titles.size() == 1) {
                String titleText = ((TitleTag) titles.elementAt(0)).getTitle();
                if (titleText != null && ((titleText.indexOf("blog") > -1) || titleText.indexOf("weblog") > -1)) {
                    return true;
                }
            }

            // now let's see there is a link for major blog publishing frameworks, within the page
            hasLinkToBlogFramework(pageURL);

        } catch (ParserException e) {
            e.printStackTrace();
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }

        return false;
    }

    private boolean isKnownBlog(String hostAddress) {
        for (int i = 0; i < knownBlogURLList.size(); i++) {
            if (hostAddress.indexOf(knownBlogURLList.get(i)) > -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param pageURL
     * @return
     */
    private boolean hasLinkToBlogFramework(URL pageURL) {
        try {
            Page page = new Page(pageURL.openConnection());
            Parser parser = new Parser(new Lexer(page));
            TagNameFilter f2 = new TagNameFilter("a");

            NodeList nl = parser.parse(f2);

            for (int i = 0; i < nl.size(); i++) {
                Node node = nl.elementAt(i);
                LinkTag tag = (LinkTag) node;
                String url = tag.getLink();
                if(blogPublishingFrameworks.get(url) != null){
                    return true;
                }
            }
        } catch (ParserException e) {
            return false;

        } catch (IOException e) {
            return false;

        }

        return false;
    }
}
