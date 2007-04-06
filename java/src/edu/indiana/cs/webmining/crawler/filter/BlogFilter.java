package edu.indiana.cs.webmining.crawler.filter;

import java.util.logging.Level;

import javax.management.AttributeNotFoundException;

import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.framework.Filter;
import org.archive.crawler.settings.ComplexType;
import org.archive.crawler.settings.MapType;

import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.blog.BlogDetector;

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
        CrawlURI curi = (o instanceof CrawlURI) ? (CrawlURI) o : null;
        return (detector.identifyURL(curi.getBaseURI().toString(), null) == Constants.BLOG);
    }

    
}
