/* HashCrawlMapper
 * 
 * Created on Sep 30, 2005
 *
 * Copyright (C) 2005 Internet Archive.
 * 
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 * 
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 * 
 * Heritrix is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.crawler.processor;

import java.util.regex.Matcher;

import org.archive.crawler.datamodel.CandidateURI;
import org.archive.crawler.settings.SimpleType;
import org.archive.util.TextUtils;

import st.ata.util.FPGenerator;

/**
 * Maps URIs to one of N crawler names by applying a hash to the
 * URI's (possibly-transformed) classKey. 
 * 
 * @author gojomo
 * @version $Date: 2006-09-25 20:19:54 +0000 (Mon, 25 Sep 2006) $, $Revision: 4654 $
 */
public class HashCrawlMapper extends CrawlMapper {
    private static final long serialVersionUID = 1L;
    
    /** count of crawlers */
    public static final String ATTR_CRAWLER_COUNT = "crawler-count";
    public static final Long DEFAULT_CRAWLER_COUNT = new Long(1);

    /** regex pattern for reducing classKey */
    public static final String ATTR_REDUCE_PATTERN = "reduce-prefix-pattern";
    public static final String DEFAULT_REDUCE_PATTERN = "";
    
//    /** replace pattern for reducing classKey */
//    public static final String ATTR_REPLACE_PATTERN = "replace-pattern";
//    public static final String DEFAULT_REPLACE_PATTERN = "";
 
    long bucketCount = 1;
    String reducePattern = null;
//    String replacePattern = null;
 
    /**
     * Constructor.
     * @param name Name of this processor.
     */
    public HashCrawlMapper(String name) {
        super(name, "HashCrawlMapper. Maps URIs to a numerically named " +
                "crawler by hashing the URI's (possibly transfored) " +
                "classKey to one of the specified number of buckets.");
        addElementToDefinition(new SimpleType(ATTR_CRAWLER_COUNT,
            "Number of crawlers among which to split up the URIs. " +
            "Their names are assumed to be 0..N-1.",
            DEFAULT_CRAWLER_COUNT));
        addElementToDefinition(new SimpleType(ATTR_REDUCE_PATTERN,
                "A regex pattern to apply to the classKey, using " +
                "the first match as the mapping key. If empty (the" +
                "default), use the full classKey.",
                DEFAULT_REDUCE_PATTERN));
    }

    /**
     * Look up the crawler node name to which the given CandidateURI 
     * should be mapped. 
     * 
     * @param cauri CandidateURI to consider
     * @return String node name which should handle URI
     */
    protected String map(CandidateURI cauri) {
        // get classKey, via frontier to generate if necessary
        String key = getController().getFrontier().getClassKey(cauri);
        return mapString(key, reducePattern, bucketCount); 
    }

    protected void initialTasks() {
        super.initialTasks();
        bucketCount = (Long) getUncheckedAttribute(null,ATTR_CRAWLER_COUNT);
        kickUpdate();
    }

    @Override
    public void kickUpdate() {
        super.kickUpdate();
        reducePattern = (String)getUncheckedAttribute(null, ATTR_REDUCE_PATTERN);
    }
    
    public static String mapString(String key, String reducePattern, long bucketCount) {
        if(reducePattern!=null && reducePattern.length()>0) {
           Matcher matcher = TextUtils.getMatcher(reducePattern,key);
           if(matcher.find()) {
               key = matcher.group();
           }
           TextUtils.recycleMatcher(matcher);
        }
        long fp = FPGenerator.std64.fp(key);
        long bucket = fp % bucketCount;
        return Long.toString(bucket >= 0 ? bucket : -bucket);
    }
}