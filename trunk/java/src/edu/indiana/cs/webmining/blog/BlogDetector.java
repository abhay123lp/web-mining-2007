/**
 * Copyright (C) 2007 The Trustees of Indiana University. All rights reserved.
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
package edu.indiana.cs.webmining.blog;

import edu.indiana.cs.webmining.Constants;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.InputStreamSource;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BlogDetector {

    private static BlogDetector ourInstance = new BlogDetector();

    private Logger logger = Logger.getLogger(BlogProcessingSystem.SYSTEM_NAME);

    private static Map<String, Integer> knownBlogURLList;
    private static Map<String, Integer> history;
    private static Map<String, Boolean> blogPublishingFrameworks;

    /**
     * Some blogs urls appear to be pointing to blogs, but they are not blogs actually. For example,
     * a links that contains www.bloglines.com but doesn't contain /blog part is not a blog. It is
     * just a link to the bloglines.com site. So we need to remove them being identified as blogs.
     */
    private static Map<String, String> deceivingNonBlogs;
    private static final String APPLICATION_RSS_XML_TYPE = "application/rss+xml";

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
        knownBlogURLList = new HashMap<String, Integer>();
        knownBlogURLList.put("blogspot.com", Constants.BLOG);
        knownBlogURLList.put("blog.myspace.com", Constants.BLOG);
        knownBlogURLList.put("blogger.com", Constants.BLOG);
        knownBlogURLList.put("weblogs.com", Constants.BLOG);
        knownBlogURLList.put("diaryland.com", Constants.BLOG);
        knownBlogURLList.put("livejournal.com", Constants.BLOG);
        knownBlogURLList.put("journalspace.com", Constants.BLOG);
        knownBlogURLList.put("blogalia.com", Constants.BLOG);
        knownBlogURLList.put("pitas.com", Constants.BLOG);
        knownBlogURLList.put("persianblog.com", Constants.BLOG);
        knownBlogURLList.put("bpersianlog.com", Constants.BLOG);
        knownBlogURLList.put("diaryhub.com", Constants.BLOG);
        knownBlogURLList.put("diaryhub.net", Constants.BLOG);
        knownBlogURLList.put("radio.weblogs.com", Constants.BLOG);
        knownBlogURLList.put("blogs.law.harvard.edu", Constants.BLOG);
        knownBlogURLList.put("blogs.it", Constants.BLOG);
        knownBlogURLList.put("manilasites.com", Constants.BLOG);
        knownBlogURLList.put("editthispage.com", Constants.BLOG);
        knownBlogURLList.put("weblogger.com", Constants.BLOG);
        knownBlogURLList.put("typepad", Constants.BLOG);
        knownBlogURLList.put("twoday.net", Constants.BLOG);
        knownBlogURLList.put("blogs.salon.com", Constants.BLOG);
        knownBlogURLList.put("blogs.salon.com", Constants.BLOG);
        knownBlogURLList.put("jroller.com", Constants.BLOG);
        knownBlogURLList.put("diarist.com", Constants.BLOG);
        knownBlogURLList.put("antville.org", Constants.BLOG);
        knownBlogURLList.put("bloggingnetwork.com", Constants.BLOG);
        knownBlogURLList.put("crimsonblog.com", Constants.BLOG);
        knownBlogURLList.put("skyblog.com", Constants.BLOG);
        knownBlogURLList.put("wordpress.com", Constants.BLOG);
        knownBlogURLList.put("blog.pl", Constants.BLOG);
        knownBlogURLList.put("e-blog.pl", Constants.BLOG);
        knownBlogURLList.put("weblog.pl", Constants.BLOG);
        knownBlogURLList.put("monblogue.com", Constants.BLOG);
        knownBlogURLList.put("joueb.com", Constants.BLOG);
        knownBlogURLList.put("blogstudio.com", Constants.BLOG);
        knownBlogURLList.put("blog-city.com", Constants.BLOG);
        knownBlogURLList.put("blogsky.com", Constants.BLOG);
        knownBlogURLList.put("u-blog.net", Constants.BLOG);
        knownBlogURLList.put("bbarrapunto.com", Constants.BLOG);
        knownBlogURLList.put("blig", Constants.BLOG);
        knownBlogURLList.put("g-blog.net", Constants.BLOG);
        knownBlogURLList.put("babelogue.citypages.com", Constants.BLOG);
        knownBlogURLList.put("jevon.org", Constants.BLOG);
        knownBlogURLList.put("tripod.com", Constants.BLOG);
        knownBlogURLList.put("spaces.live.com", Constants.BLOG);
        knownBlogURLList.put("1060.org/blogxter", Constants.BLOG);

        blogPublishingFrameworks = new HashMap<String, Boolean>();
        blogPublishingFrameworks.put("http://www.sixapart.com/movabletype/", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.movabletype.org/", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.modblog.com", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.feedblitz.com", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.lifetype.net/", Boolean.TRUE);
        blogPublishingFrameworks.put("http://www.wordpress.com/", Boolean.TRUE);

        history = new HashMap<String, Integer>();

        deceivingNonBlogs = new HashMap<String, String>();
        deceivingNonBlogs.put("www.bloglines.com", "www.bloglines.com/blog/");

        logger.fine("Blog Detection System initialized ......");

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
     * @param inputStream
     * @return
     */
    public int identifyURL(String pageURL, InputStream inputStream) {
        try {
            // first let's avoid traps. .
            if (pageURL == null || "".equals(pageURL)) {
                return Constants.NOT_A_BLOG;
            }

            // first let's check in the cache
            Integer urlType = history.get(pageURL);

            if (urlType == null) {
                // cache miss
                urlType = identifyURL(new URL(pageURL), inputStream);
                history.put(pageURL, urlType);
            }

            return urlType;
        } catch (MalformedURLException e) {
            logger.fine("Malformed URL " + pageURL + " passed for blog identification " + e.getMessage());
            return Constants.NOT_A_BLOG;
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
     * @param pageURL     - url of the page
     * @param inputStream - input stream to the web page under consideration. If you pass null to this, this method will
     *                    open a connection to the page if required.
     * @return
     */
    public int identifyURL(URL pageURL, InputStream inputStream) {
        int status = -1;

        // sorry, we do not handle anything other than http. Can there be smtp or tcp blogs?
        if (!"http".equals(pageURL.getProtocol())) {
            return Constants.NOT_A_BLOG;
        }

        // First let's look at the blog address.
        // let's see whether this is a url appears to be blog, but not really a blog
//        for(String blogHost : )

        // Let's see whether this is a known blog
        if ((status = getBlogId(pageURL)) != 0) {
            return status;
        }

        // now let's see whether the url contains, blog as a word
        if (pageURL.getHost().contains("blog") || pageURL.getFile().contains("blog")) {
            return Constants.BLOG;
        }

        // hmm, now surface scans are over. Let's look at the page now.

        try {
// first let's see whether title has XX's blog in it
            Page page;
            if (inputStream == null) {
                page = new Page(pageURL.openConnection());
            } else {
                page = new Page(new InputStreamSource(inputStream));
            }
            Parser parser = new Parser(new Lexer(page));
            TagNameFilter titleFilter = new TagNameFilter("title");
            NodeList titles = parser.parse(titleFilter);

            if (titles.size() == 1) {
                String titleText = ((TitleTag) titles.elementAt(0)).getTitle();
                if (titleText != null && ((titleText.indexOf("blog") > -1) || titleText.indexOf("weblog") > -1)) {
                    return Constants.BLOG;
                }
            }

            // now let's see there is a link for major blog publishing frameworks or has an RSS feed, within the page
            return hasLinkToBlogFramework(pageURL);

        } catch (ParserException e) {
            logger.fine("Parsing Exception occurred for URL " + pageURL + "error --> " + e.getMessage());
            return Constants.NOT_A_BLOG;

        } catch (IOException e) {
            logger.fine("IO Exception occurred for URL " + pageURL + "error --> " + e.getMessage());
            return Constants.NOT_A_BLOG;

        }

    }

    private int getBlogId(URL pageURL) {
        String hostAddress = pageURL.getHost();

        if (deceivingNonBlogs.containsKey(hostAddress)) {
            return (pageURL.toString().contains(deceivingNonBlogs.get(hostAddress))) ? Constants.BLOG : Constants.NOT_A_BLOG;
        }

        for (String blogName : knownBlogURLList.keySet()) {
            if (hostAddress.indexOf(blogName) > -1) {
                return Constants.BLOG;
            }
        }

        return 0;
    }

    /**
     * @param pageURL
     * @return The blog id or -1 if it is not a blog.
     */
    private int hasLinkToBlogFramework(URL pageURL) {
        try {
            Page page = new Page(pageURL.openConnection());
            Parser parser = new Parser(new Lexer(page));
            TagNameFilter linkTag = new TagNameFilter("link");

            NodeList nodeList = parser.parse(linkTag);

            TagNode tag;
            for (int i = 0; i < nodeList.size(); i++) {
                tag = (TagNode) nodeList.elementAt(i);
                if (APPLICATION_RSS_XML_TYPE.equalsIgnoreCase(tag.getAttribute("type"))) {
                    return Constants.BLOG;
                }
            }
            TagNameFilter aTag = new TagNameFilter("a");
            NodeList nl = parser.parse(aTag);

            for (int i = 0; i < nl.size(); i++) {
                Node node = nl.elementAt(i);
                LinkTag linkTagNode = (LinkTag) node;
                String url = linkTagNode.getLink();
                if (url != null && (url.contains("feed:") || blogPublishingFrameworks.get(url) != null)) {
                    return Constants.BLOG;
                }
            }
        } catch (ParserException e) {
            logger.fine("Parsing Exception occurred for URL " + pageURL + "error --> " + e.getMessage());
            return Constants.NOT_A_BLOG;

        } catch (IOException e) {
            logger.fine("IO Exception occurred for URL " + pageURL + "error --> " + e.getMessage());
            return Constants.NOT_A_BLOG;

        }

        return Constants.NOT_A_BLOG;
    }

    public static void main(String[] args) {
        BlogDetector blogDetector = new BlogDetector();
        int result = blogDetector.identifyURL("http://zeldman.com/", null);
        System.out.println("result = " + result);
    }
}
