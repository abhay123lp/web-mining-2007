/* $Id: ExperimentalWARCWriterProcessor.java 4935 2007-02-23 00:27:24Z gojomo $
 *
 * Created on August 1st, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.crawler.writer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.datamodel.FetchStatusCodes;
import org.archive.crawler.deciderules.recrawl.IdenticalDigestDecideRule;
import org.archive.crawler.event.CrawlStatusListener;
import org.archive.crawler.extractor.Link;
import org.archive.crawler.framework.WriterPoolProcessor;
import org.archive.crawler.settings.SimpleType;
import org.archive.crawler.settings.Type;
import org.archive.io.WriterPoolMember;
import org.archive.io.WriterPoolSettings;
import org.archive.io.warc.ExperimentalWARCWriter;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCWriterPool;
import org.archive.uid.GeneratorFactory;
import org.archive.util.ArchiveUtils;
import org.archive.util.anvl.ANVLRecord;

/**
 * Experimental WARCWriterProcessor.
 * Goes against the pending release of 0.12 of the WARC specification, the
 * "Marcel Marceau" release. See <a href="https://archive-access.svn.sourceforge.net/svnroot/archive-access/branches/gjm_warc_0_12/warc/warc_file_format.html">latest revision</a>
 * for current state.  The 0.10 WARC implemenation has been moved to
 * {@link ExperimentalV10WARCWriterProcessor}.
 * 
 * <p>TODO: Remove ANVLRecord. Rename NameValue or use RFC822
 * (commons-httpclient?) or find something else.
 * 
 * @author stack
 */
public class ExperimentalWARCWriterProcessor extends WriterPoolProcessor
implements CoreAttributeConstants, CrawlStatusListener,
WriterPoolSettings, FetchStatusCodes, WARCConstants {
    private static final long serialVersionUID = 6182850087635847443L;

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Key for whether to write 'request' type records where possible
     */
    public static final String ATTR_WRITE_REQUESTS =
        "write-requests";
    
    /**
     * Key for whether to write 'metadata' type records where possible
     */
    public static final String ATTR_WRITE_METADATA =
        "write-metadata";
    
    /**
     * Key for whether to write 'revisit' type records when
     * consecutive identical digest
     */
    public static final String ATTR_WRITE_REVISIT_FOR_IDENTICAL_DIGESTS =
        "write-revisit-for-identical-digests";
    
    /**
     * Key for whether to write 'revisit' type records for server
     * "304 not modified" responses
     */
    public static final String ATTR_WRITE_REVISIT_FOR_NOT_MODIFIED =
        "write-revisit-for-not-modified";
    
    /**
     * Default path list.
     */
    private static final String [] DEFAULT_PATH = {"warcs"};

    protected String [] getDefaultPath() {
        return DEFAULT_PATH;
    }
    
    /**
     * @param name Name of this writer.
     */
    public ExperimentalWARCWriterProcessor(final String name) {
        super(name, "Experimental WARCWriter processor (Version 0.12)");
        Type e = addElementToDefinition(
                new SimpleType(ATTR_WRITE_REQUESTS,
                "Whether to write 'request' type records. " +
                "Default is true.", new Boolean(true)));
        e.setOverrideable(true);
        e.setExpertSetting(true);
        e = addElementToDefinition(
                new SimpleType(ATTR_WRITE_METADATA,
                "Whether to write 'metadata' type records. " +
                "Default is true.", new Boolean(true)));
        e.setOverrideable(true);
        e.setExpertSetting(true);
        e = addElementToDefinition(
                new SimpleType(ATTR_WRITE_REVISIT_FOR_IDENTICAL_DIGESTS,
                "Whether to write 'revisit' type records when a URI's " +
                "history indicates the previous fetch had an identical " +
                "content digest. " +
                "Default is true.", new Boolean(true)));
        e.setOverrideable(true);
        e.setExpertSetting(true);
        e = addElementToDefinition(
                new SimpleType(ATTR_WRITE_REVISIT_FOR_NOT_MODIFIED,
                "Whether to write 'revisit' type records when a " +
                "304-Not Modified response is received. " +
                "Default is true.", new Boolean(true)));
        e.setOverrideable(true);
        e.setExpertSetting(true);
    }

    protected void setupPool(final AtomicInteger serialNo) {
		setPool(new WARCWriterPool(serialNo, this, getPoolMaximumActive(),
            getPoolMaximumWait()));
    }
    
    /**
     * Writes a CrawlURI and its associated data to store file.
     * 
     * Currently this method understands the following uri types: dns, http, and
     * https.
     * 
     * @param curi CrawlURI to process.
     * 
     */
    protected void innerProcess(CrawlURI curi) {
        // If failure, or we haven't fetched the resource yet, return
        if (curi.getFetchStatus() <= 0) {
            return;
        }
        
        // If no content, don't write record.
        int recordLength = (int)curi.getContentSize();
        if (recordLength <= 0) {
        	// Write nothing.
        	return;
        }
        
        String scheme = curi.getUURI().getScheme().toLowerCase();
        try {
            if (shouldWrite(curi)) {
                write(scheme, curi);
            } else {
                logger.info("This writer does not write out scheme " +
                        scheme + " content");
            }
        } catch (IOException e) {
            curi.addLocalizedError(this.getName(), e, "WriteRecord: " +
                curi.toString());
            logger.log(Level.SEVERE, "Failed write of Record: " +
                curi.toString(), e);
        }
    }
    
    protected void write(final String lowerCaseScheme, final CrawlURI curi)
    throws IOException {
        WriterPoolMember writer = getPool().borrowFile();
        long position = writer.getPosition();
        // See if we need to open a new file because we've exceeed maxBytes.
        // Call to checkFileSize will open new file if we're at maximum for
        // current file.
        writer.checkSize();
        if (writer.getPosition() != position) {
            // We just closed the file because it was larger than maxBytes.
            // Add to the totalBytesWritten the size of the first record
            // in the file, if any.
            setTotalBytesWritten(getTotalBytesWritten() +
            	(writer.getPosition() - position));
            position = writer.getPosition();
        }
        
        ExperimentalWARCWriter w = (ExperimentalWARCWriter)writer;
        try {
            // Write a request, response, and metadata all in the one
            // 'transaction'.
            final URI baseid = getRecordID();
            final String timestamp =
                ArchiveUtils.getLog14Date(curi.getLong(A_FETCH_BEGAN_TIME));
            if (lowerCaseScheme.startsWith("http")) {
                // Add named fields for ip, checksum, and relate the metadata
                // and request to the resource field.
                // TODO: Use other than ANVL (or rename ANVL as NameValue or
                // use RFC822 (commons-httpclient?).
                ANVLRecord headers = new ANVLRecord(5);
                if (curi.getContentDigest() != null) {
                    headers.addLabelValue(HEADER_KEY_CHECKSUM,
                        curi.getContentDigestSchemeString());
                }
                headers.addLabelValue(HEADER_KEY_IP, getHostAddress(curi));
                URI rid;
                
                if (IdenticalDigestDecideRule.hasIdenticalDigest(curi) && 
                        ((Boolean)getUncheckedAttribute(curi, 
                                ATTR_WRITE_REVISIT_FOR_IDENTICAL_DIGESTS))) {
                    rid = writeRevisitDigest(w, timestamp, HTTP_RESPONSE_MIMETYPE,
                            baseid, curi, headers);
                } else if (curi.getFetchStatus() == HttpStatus.SC_NOT_MODIFIED && 
                        ((Boolean)getUncheckedAttribute(curi, 
                                ATTR_WRITE_REVISIT_FOR_NOT_MODIFIED))) {
                    rid = writeRevisitNotModified(w, timestamp,
                            baseid, curi, headers);
                } else {
                    if (curi.isTruncatedFetch()) {
                        String value = curi.isTimeTruncatedFetch()?
                            NAMED_FIELD_TRUNCATED_VALUE_TIME:
                            curi.isLengthTruncatedFetch()?
                                NAMED_FIELD_TRUNCATED_VALUE_LEN:
                                curi.isHeaderTruncatedFetch()?
                                    NAMED_FIELD_TRUNCATED_VALUE_HEAD:
                            // TODO: Add this to spec.
                            TRUNCATED_VALUE_UNSPECIFIED;
                        headers.addLabelValue(HEADER_KEY_TRUNCATED, value);
                    }
                    rid = writeResponse(w, timestamp, HTTP_RESPONSE_MIMETYPE,
                    	baseid, curi, headers);
                }
                
                headers = new ANVLRecord(1);
                headers.addLabelValue(HEADER_KEY_CONCURRENT_TO,
                    '<' + rid.toString() + '>');

                if(((Boolean)getUncheckedAttribute(curi, ATTR_WRITE_REQUESTS))) {
                    writeRequest(w, timestamp, HTTP_REQUEST_MIMETYPE,
                            baseid, curi, headers);
                }
                if(((Boolean)getUncheckedAttribute(curi, ATTR_WRITE_METADATA))) {
                    writeMetadata(w, timestamp, baseid, curi, headers);
                } 
            } else if (lowerCaseScheme.equals("dns")) {
                ANVLRecord headers = null;
                String ip = curi.getString(A_DNS_SERVER_IP_LABEL);
                if (ip != null && ip.length() > 0) {
                    headers = new ANVLRecord(1);
                    headers.addLabelValue(HEADER_KEY_IP, ip);
                }
                writeResponse(w, timestamp, curi.getContentType(), baseid,
                    curi, headers);
            } else {
                logger.warning("No handler for scheme " + lowerCaseScheme);
            }
        } catch (IOException e) {
            // Invalidate this file (It gets a '.invalid' suffix).
            getPool().invalidateFile(writer);
            // Set the writer to null otherwise the pool accounting
            // of how many active writers gets skewed if we subsequently
            // do a returnWriter call on this object in the finally block.
            writer = null;
            throw e;
        } finally {
            if (writer != null) {
            	setTotalBytesWritten(getTotalBytesWritten() +
            	     (writer.getPosition() - position));
                getPool().returnFile(writer);
            }
        }
        checkBytesWritten();
    }
    
    protected URI writeRequest(final ExperimentalWARCWriter w,
            final String timestamp, final String mimetype,
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        final URI uid = qualifyRecordID(baseid, TYPE, REQUEST);
        w.writeRequestRecord(curi.toString(), timestamp, mimetype, uid,
            namedFields,
            curi.getHttpRecorder().getRecordedOutput().getReplayInputStream(),
            curi.getHttpRecorder().getRecordedOutput().getSize());
        return uid;
    }
    
    protected URI writeResponse(final ExperimentalWARCWriter w,
            final String timestamp, final String mimetype,
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        w.writeResponseRecord(curi.toString(), timestamp, mimetype, baseid,
            namedFields,
            curi.getHttpRecorder().getRecordedInput().getReplayInputStream(),
            curi.getHttpRecorder().getRecordedInput().getSize());
        return baseid;
    }
    
    protected URI writeRevisitDigest(final ExperimentalWARCWriter w,
            final String timestamp, final String mimetype,
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        long revisedLength = curi.getHttpRecorder().getRecordedInput().getContentBegin();
        revisedLength = revisedLength > 0 
            ? revisedLength 
            : curi.getHttpRecorder().getRecordedInput().getSize();
        namedFields.addLabelValue(
        		HEADER_KEY_PROFILE, PROFILE_REVISIT_IDENTICAL_DIGEST);
        namedFields.addLabelValue(
        		HEADER_KEY_TRUNCATED, NAMED_FIELD_TRUNCATED_VALUE_LEN);
        w.writeRevisitRecord(curi.toString(), timestamp, mimetype, baseid,
            namedFields,
            curi.getHttpRecorder().getRecordedInput().getReplayInputStream(),
            revisedLength);
        return baseid;
    }
    
    protected URI writeRevisitNotModified(final ExperimentalWARCWriter w,
            final String timestamp, 
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        namedFields.addLabelValue(
        		HEADER_KEY_PROFILE, PROFILE_REVISIT_NOT_MODIFIED);
        // save just enough context to understand basis of not-modified
        if(curi.containsKey(A_HTTP_TRANSACTION)) {
            HttpMethodBase method = 
                (HttpMethodBase) curi.getObject(A_HTTP_TRANSACTION);
            saveHeader(A_ETAG_HEADER,method,namedFields,HEADER_KEY_ETAG);
            saveHeader(A_LAST_MODIFIED_HEADER,method,namedFields,
            		HEADER_KEY_LAST_MODIFIED);
        }
        // truncate to zero-length (all necessary info is above)
        namedFields.addLabelValue(HEADER_KEY_TRUNCATED, NAMED_FIELD_TRUNCATED_VALUE_LEN);
        w.writeRevisitRecord(curi.toString(), timestamp, null, baseid,
            namedFields,
            curi.getHttpRecorder().getRecordedInput().getReplayInputStream(),
            0);
        return baseid;
    }
    
    /**
     * Save a header from the given HTTP operation into the 
     * provider headers under a new name
     * 
     * @param origName header name to get if present
     * @param method http operation containing headers
     */
    protected void saveHeader(String origName, HttpMethodBase method, 
    		ANVLRecord headers, String newName) {
        Header header = method.getResponseHeader(origName);
        if(header!=null) {
            headers.addLabelValue(newName, header.getValue());
        }
    }

	protected URI writeMetadata(final ExperimentalWARCWriter w,
            final String timestamp,
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        final URI uid = qualifyRecordID(baseid, TYPE, METADATA);
        // Get some metadata from the curi.
        // TODO: Get all curi metadata.
        // TODO: Use other than ANVL (or rename ANVL as NameValue or use
        // RFC822 (commons-httpclient?).
        ANVLRecord r = new ANVLRecord();
        if (curi.isSeed()) {
            r.addLabel("seed");
        } else {
        	if (curi.forceFetch()) {
        		r.addLabel("force-fetch");
        	}
            r.addLabelValue("via", curi.flattenVia());
            r.addLabelValue("pathFromSeed", curi.getPathFromSeed());
        }
        
        // Add outlinks though they are effectively useless without anchor text.
        Collection<Link> links = curi.getOutLinks();
        if (links != null || links.size() > 0) {
            for (Link link: links) {
                r.addLabelValue("outlink", link.toString());
            }
        }
        
        // TODO: Other curi fields to write to metadata.
        // 
        // Credentials
        // 
        // fetch-began-time: 1154569278774
        // fetch-completed-time: 1154569281816
        //
        // Annotations.
        
        byte [] b = r.getUTF8Bytes();
        w.writeMetadataRecord(curi.toString(), timestamp, ANVLRecord.MIMETYPE,
            uid, namedFields, new ByteArrayInputStream(b), b.length);
        return uid;
    }
    
    protected URI getRecordID() throws IOException {
        URI result;
        try {
            result = GeneratorFactory.getFactory().getRecordID();
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
        return result;
    }
    
    protected URI qualifyRecordID(final URI base, final String key,
            final String value)
    throws IOException {
        URI result;
        Map<String, String> qualifiers = new HashMap<String, String>(1);
        qualifiers.put(key, value);
        try {
            result = GeneratorFactory.getFactory().
                qualifyRecordID(base, qualifiers);
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
        return result;
    }  
    
    @Override
    protected String getFirstrecordStylesheet() {
        return "/warcinfobody.xsl";
    }
}