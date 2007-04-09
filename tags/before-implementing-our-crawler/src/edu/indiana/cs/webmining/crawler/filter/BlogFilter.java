package edu.indiana.cs.webmining.crawler.filter;

import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.blog.BlogDetector;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.framework.Filter;

import java.io.IOException;

public class BlogFilter extends Filter {

    private BlogDetector detector;

    public BlogFilter(String name, String description) {
        this(name);
        setDescription(description);
    }

    public BlogFilter(String name) {
        super(name,
                "BlogFilter. A filter that accepts " +
                        "URIs deemed to contain a blog");
        // TODO Auto-generated constructor stub
        detector = BlogDetector.getInstance();
    }

    protected boolean innerAccepts(Object o) {
        try {
            if (o instanceof CrawlURI) {
                CrawlURI crawlURI = (CrawlURI) o;
                return (detector.identifyURL(crawlURI.getBaseURI().toString(), null) == Constants.BLOG);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return false;
    }


}