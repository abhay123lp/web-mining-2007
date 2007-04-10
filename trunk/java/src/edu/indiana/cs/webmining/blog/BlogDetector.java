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
import edu.indiana.cs.webmining.blog.impl.BlogDBManager;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.RegexFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.InputStreamSource;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import spider.util.Hashing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class BlogDetector {

    private static BlogDetector ourInstance = new BlogDetector();

    private static Map<String, Integer> knownBlogURLList;
    private static Map<String, Integer> history;
    private static Map<String, Boolean> blogPublishingFrameworks;

    private static Hashtable<String, Boolean> knowsMediaFiles;


    /**
     * Some blogs urls appear to be pointing to blogs, but they are not blogs actually. For example,
     * a links that contains www.bloglines.com but doesn't contain /blog part is not a blog. It is
     * just a link to the bloglines.com site. So we need to remove them being identified as blogs.
     */
    private static Map<String, String> deceivingNonBlogs;
    private static final String APPLICATION_RSS_XML_TYPE = "application/rss+xml";
    private static final String APPLICATION_ATOM_XML_TYPE = "application/atom+xml";
    private File tempFolder = new File("tmp-crawled-pages");
    private HttpClient client;

    BlogDBManager dbManager;

    public static BlogDetector getInstance() {
        return ourInstance;
    }

    private BlogDetector() {
        intialize();

        try {
            dbManager = new BlogDBManager();
        } catch (IOException e) {
            e.printStackTrace();

        }
//        initHistory();

//        Runtime.getRuntime().addShutdownHook(new ShutDownHook(this));
    }

    public boolean isMediaFile(String pageURL) {
        if (pageURL.lastIndexOf(".") == (pageURL.length() - 4)) {
            String fileExtension = pageURL.substring(pageURL.lastIndexOf(".") + 1, pageURL.length());
            return knowsMediaFiles.contains(fileExtension);
        } else {
            return false;
        }
    }

//    private void initHistory() {
//        Properties props = new Properties();
//        try {
//
//            props.load(new FileInputStream(new File(BlogProcessor.BLOG_DETECTION_PROPERTIES)));
//            blogHistoryFileName = new File(props.getProperty("blog-history"));
//
//            if (blogHistoryFileName.isFile()) {
//                BufferedReader in = new BufferedReader(new FileReader(blogHistoryFileName));
//                String blogURL;
//                while ((blogURL = in.readLine()) != null) {
//                    history.put(blogURL, Constants.BLOG);
//                }
//                in.close();
//
//            } else {
//                blogHistoryFileName.createNewFile();
//            }
//
//
//        } catch (IOException e) {
//            logger.info("Can not load blog history.");
//            e.printStackTrace();
//        }
//    }

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
        knownBlogURLList.put("wired.com", Constants.BLOG);
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


        deceivingNonBlogs = new HashMap<String, String>();
        deceivingNonBlogs.put("www.bloglines.com", "www.bloglines.com/blog/");

        knowsMediaFiles = new Hashtable<String, Boolean>();
        knowsMediaFiles.put("mp3", Boolean.TRUE);
        knowsMediaFiles.put("pdf", Boolean.TRUE);
        knowsMediaFiles.put("mov", Boolean.TRUE);
        knowsMediaFiles.put("avi", Boolean.TRUE);
        knowsMediaFiles.put("jpg", Boolean.TRUE);
        knowsMediaFiles.put("JPG", Boolean.TRUE);
        knowsMediaFiles.put("gif", Boolean.TRUE);
        knowsMediaFiles.put("GIF", Boolean.TRUE);
        knowsMediaFiles.put("png", Boolean.TRUE);
        knowsMediaFiles.put("fla", Boolean.TRUE);

        if (!tempFolder.isDirectory()) {
            tempFolder.mkdir();
        }

        System.out.println("Blog Detection System initialized ......");

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
     * @param htmlFile
     * @return
     */
    public int identifyURL(String pageURL, File htmlFile) throws IOException {
        Integer urlType = Constants.NOT_A_BLOG;
        try {
            // first let's avoid traps. .
            if (pageURL == null || "".equals(pageURL) || isMediaFile(pageURL) || !pageURL.startsWith("http://")) {
                urlType = Constants.NOT_A_BLOG;
            } else {

                // first let's check in the cache
                urlType = dbManager.getURLType(pageURL);

                if (urlType == null) {
                    // cache miss
                    urlType = identifyURL(new URL(pageURL), htmlFile);
                    dbManager.addURLType(pageURL, urlType);
                }
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Constants.NOT_A_BLOG;
        } catch (SQLException e) {
            e.printStackTrace();
            return Constants.NOT_A_BLOG;
        }
        return urlType;
    }

    /**
     * This will detect whether a URL given  is pointed to a blog or not. There are striclty defined set of rules
     * to identify blogs as of now. But this indentification is based on http://search.cpan.org/src/MCEGLOWS/WWW-Blog-Identify-0.06/Identify.pm
     * <p/>
     * This algorithm said to be favouring false negatives over false positives.
     * <p/>
     * We might improve this algorithm as and when we find interesting points.
     *
     * @param pageURL  - url of the page
     * @param htmlFile - pointer to the web page under consideration. If you pass null to this, this method will
     *                 open a connection to the page if required.
     * @return
     */
    public synchronized int identifyURL(URL pageURL, File htmlFile) {
        int status = -1;

        // sorry, we do not handle anything other than http. Can there be smtp or tcp blogs?
        if (!"http".equals(pageURL.getProtocol()) || isMediaFile(pageURL.getFile())) {
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
            if (htmlFile == null) {
                htmlFile = fetchAndSaveFile(pageURL);
            }

            if (htmlFile == null) {
                return Constants.NOT_A_BLOG;
            }

            page = new Page(new InputStreamSource(new FileInputStream(htmlFile)));
            Parser parser = new Parser(new Lexer(page));
            TagNameFilter titleFilter = new TagNameFilter("title");
            NodeList titles = parser.parse(titleFilter);

            if (titles.size() == 1) {
                String titleText = ((TitleTag) titles.elementAt(0)).getTitle();
                if (titleText != null && ((titleText.indexOf("blog") > -1) || titleText.indexOf("weblog") > -1)) {
                    return Constants.BLOG;
                }
            }

            page.close();
            // now let's see there is a link for major blog publishing frameworks or has an RSS feed, within the page
            return hasLinkToBlogFramework(htmlFile, pageURL.toString());

        } catch (ParserException e) {
            dbManager.setBlogProcessingFailed(pageURL.toString());
            System.out.println("Parsing Exception occurred for URL " + pageURL + "error --> " + e.getMessage());
            return Constants.NOT_A_BLOG;

        } catch (IOException e) {
            dbManager.setBlogProcessingFailed(pageURL.toString());
            e.printStackTrace();
            return Constants.NOT_A_BLOG;

        } catch (BlogCrawlingException e) {
            dbManager.setBlogProcessingFailed(pageURL.toString());
            e.printStackTrace();
            return Constants.NOT_A_BLOG;

        }

    }

    private File fetchAndSaveFile(URL pageURL) throws BlogCrawlingException {
        File htmlFile = null;
        try {

            // Read all the text returned by the server

            htmlFile = new File(tempFolder, Hashing.getHashValue(pageURL.toString()));

            if (!htmlFile.isFile()) htmlFile.createNewFile();

            BufferedReader in = new BufferedReader(new InputStreamReader(pageURL.openStream()));
            BufferedWriter out = new BufferedWriter(new FileWriter(htmlFile));
            String str;
            while ((str = in.readLine()) != null) {
                out.write(str);
            }
            in.close();
            out.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            new BlogCrawlingException(e);
        } catch (IOException e) {
            e.printStackTrace();
            new BlogCrawlingException(e);
        }
        return htmlFile;
    }

    private File fetchAndSaveFile(String urlToBeFetched) throws BlogCrawlingException {
        GetMethod method = new GetMethod(urlToBeFetched);
        File htmlFile = null;
        try {
            method.setRequestHeader(new Header(Constants.HEADER_USER_AGENT, Constants.USER_AGENT_VAL));
            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            // Execute the method.

            client = new HttpClient();

            HttpClientParams clientParams = new HttpClientParams();
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            } else {

                // Read the response body and save it
                InputStream in = method.getResponseBodyAsStream();

                if (in != null) {
                    htmlFile = new File(tempFolder, Hashing.getHashValue(urlToBeFetched));
                    if (!htmlFile.isFile()) htmlFile.createNewFile();

                    OutputStream out = new FileOutputStream(htmlFile);
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BlogCrawlingException(e);
        } finally {
            method.releaseConnection();

        }

        return htmlFile;

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
    private int hasLinkToBlogFramework(File htmlFile, String pageURL) {
        Parser parser;
        try {
            Page page = new Page(new InputStreamSource(new FileInputStream(htmlFile)));
            parser = new Parser(new Lexer(page));

            TagNameFilter linkTag = new TagNameFilter("link");

            RegexFilter regexFilter = new RegexFilter();

            NodeList nodeList = parser.parse(linkTag);

            TagNode tag;
            for (int i = 0; i < nodeList.size(); i++) {
                tag = (TagNode) nodeList.elementAt(i);
                String typeAttribute = tag.getAttribute("type");
                if (APPLICATION_RSS_XML_TYPE.equalsIgnoreCase(typeAttribute) || APPLICATION_ATOM_XML_TYPE.equalsIgnoreCase(typeAttribute)) {
                    return Constants.BLOG;
                }
            }

            page = new Page(new InputStreamSource(new FileInputStream(htmlFile)));
            parser = new Parser(new Lexer(page));
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
//            logger.fine("Parsing Exception occurred for URL " + pageURL + "error --> " + e.getMessage());
            return Constants.NOT_A_BLOG;

        } catch (IOException e) {
//            logger.fine("IO Exception occurred for URL " + pageURL + "error --> " + e.getMessage());
            return Constants.NOT_A_BLOG;

        } finally {

        }

        return Constants.NOT_A_BLOG;
    }

//    protected void finalize() throws Throwable {
//        saveHistory();
//
//    }

//    public void saveHistory() throws IOException {
//        System.out.print("Saving the history ....");
//
//        BufferedWriter out = new BufferedWriter(new FileWriter(blogHistoryFileName));
//
//        for (String blogLink : history.keySet()) {
//            if (history.get(blogLink) == Constants.BLOG) {
//                out.write(blogLink + "\n");
//            }
//        }
//
//        out.close();
//
//        System.out.println("Done.");
//    }

//    class ShutDownHook extends Thread {
//
//        private BlogDetector blogDetector;
//
//
//        public ShutDownHook(BlogDetector blogDetector) {
//            this.blogDetector = blogDetector;
//        }
//
//        public void run() {
//            try {
//                blogDetector.saveHistory();
//            } catch (IOException e) {
//                e.printStackTrace();
//
//            }
//        }
//    }
}
