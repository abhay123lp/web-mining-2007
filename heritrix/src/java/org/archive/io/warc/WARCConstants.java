/*
 * WARCConstants
 *
 * $Id: WARCConstants.java 4976 2007-03-09 13:59:07Z gojomo $
 *
 * Created on July 27th, 2006
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
package org.archive.io.warc;

import java.util.Arrays;
import java.util.List;

import org.archive.io.ArchiveFileConstants;

/**
 * WARC Constants used by WARC readers and writers.
 * Below constants are used by version 0.10 and 0.12 of WARC Reader/Writer.
 * @author stack
 * @version $Revision: 4976 $ $Date: 2007-03-09 13:59:07 +0000 (Fri, 09 Mar 2007) $
 */
public interface WARCConstants extends ArchiveFileConstants {
    /**
     * Default maximum WARC file size.
     * 1Gig.
     */
    public static final int DEFAULT_MAX_WARC_FILE_SIZE = 1024 * 1024 * 1024;
    
	/**
	 * WARC MAGIC
	 * WARC files and records begin with this sequence.
	 */
	public static final String WARC_MAGIC = "WARC/";
    public static final String WARC_010_MAGIC = "WARC/";
    
    /**
     * Hard-coded version for WARC files made with this code.
     * Setting to 0.10 because differs from 0.9 spec.  See accompanying
     * package documentation.
     */
	public static final String WARC_VERSION = "0.12";
    
    /**
     * Assumed maximum size of a Header Line.
     *
     * This 100k which seems massive but its the same as the LINE_LENGTH from
     * <code>alexa/include/a_arcio.h</code>:
     * <pre>
     * #define LINE_LENGTH     (100*1024)
     * </pre>
     */
    public static final int MAX_WARC_HEADER_LINE_LENGTH = 1024 * 100;
    public static final int MAX_LINE_LENGTH = MAX_WARC_HEADER_LINE_LENGTH;
    
    /**
     * WARC file extention.
     */
    public static final String WARC_FILE_EXTENSION = "warc";
    
    /**
     * Dot WARC file extension.
     */
    public static final String DOT_WARC_FILE_EXTENSION =
        "." + WARC_FILE_EXTENSION;
    
    public static final String DOT_COMPRESSED_FILE_EXTENSION =
        ArchiveFileConstants.DOT_COMPRESSED_FILE_EXTENSION;

    /**
     * Compressed WARC file extension.
     */
    public static final String COMPRESSED_WARC_FILE_EXTENSION =
        WARC_FILE_EXTENSION + DOT_COMPRESSED_FILE_EXTENSION;
    
    /**
     * Compressed dot WARC file extension.
     */
    public static final String DOT_COMPRESSED_WARC_FILE_EXTENSION =
        DOT_WARC_FILE_EXTENSION + DOT_COMPRESSED_FILE_EXTENSION;
    
    /**
     * Encoding to use getting bytes from strings.
     *
     * Specify an encoding rather than leave it to chance: i.e whatever the
     * JVMs encoding.  Use an encoding that gets the stream as bytes, not chars.
     * 
     * <p>TODO: ARC uses ISO-8859-1.  In general, we should use UTF-8 but we
     * probably need a single byte encoding if we're out for preserving the
     * binary data as received over the net (We probably don't want to transform
     * the supra-ASCII characters to UTF-8 before storing in ARC).  For now,
     * till we figure it, DEFAULT_ENCODING is single-byte charset -- same as
     * ARCs.
     */
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    public static final String HEADER_LINE_ENCODING = DEFAULT_ENCODING;
    
    // TODO: Revisit. 8859 isn't correct, especially if we settle on RFC822
    // headers
    public static final String WARC_HEADER_ENCODING = HEADER_LINE_ENCODING;
    
    public static final String [] HEADER_FIELD_KEYS = {
        VERSION_FIELD_KEY,
        LENGTH_FIELD_KEY,
        TYPE_FIELD_KEY,
        URL_FIELD_KEY,
        DATE_FIELD_KEY,
        RECORD_IDENTIFIER_FIELD_KEY,
        MIMETYPE_FIELD_KEY
    };
    
    /**
     * WARC Record Types.
     */
    public static final String WARCINFO = "warcinfo";
    public static final String RESPONSE = "response";
    public static final String RESOURCE = "resource";
    public static final String REQUEST = "request";
    public static final String METADATA = "metadata";
    public static final String REVISIT = "revisit";
    public static final String CONVERSION = "conversion";
    public static final String CONTINUATION = "continuation";
    
    public static final String TYPE = "type";
    
    // List of all WARC Record TYPES
    public static final String [] TYPES = {WARCINFO, RESPONSE, RESOURCE,
    	REQUEST, METADATA, REVISIT, CONVERSION, CONTINUATION};
    
    // Indices into TYPES array.
    public static final int WARCINFO_INDEX = 0;
    public static final int RESPONSE_INDEX = 1;
    public static final int RESOURCE_INDEX = 2;
    public static final int REQUEST_INDEX = 3;
    public static final int METADATA_INDEX = 4;
    public static final int REVISIT_INDEX = 5;
    public static final int CONVERSION_INDEX = 6;
    public static final int CONTINUATION_INDEX = 7;
    
    // TYPES as List.
    public static final List TYPES_LIST = Arrays.asList(TYPES);
    
    /**
     * WARC-ID
     */
    public static final String WARC_ID = WARC_MAGIC + WARC_VERSION;
    public static final String WARC_010_ID = WARC_010_MAGIC + "0.10";
        
    /**
     * Header field seperator character.
     */
    public static final char HEADER_FIELD_SEPARATOR = ' ';
    
    /**
     * WSP
     * One of a space or horizontal tab character.
     * TODO: WSP undefined.  Fix.
     */
    public static final Character [] WSP = {HEADER_FIELD_SEPARATOR, '\t'};

    /**
     * Placeholder for length in Header line.
     * Placeholder is same size as the fixed field size allocated for length,
     * 12 characters.  12 characters allows records of size almost 1TB.
     */
    public static final String PLACEHOLDER_RECORD_LENGTH_STRING =
        "000000000000";
    
    public static final String NAMED_FIELD_IP_LABEL = "IP-Address";
    public static final String NAMED_FIELD_CHECKSUM_LABEL = "Checksum";
    public static final String NAMED_FIELD_RELATED_LABEL = "References";
    public static final String NAMED_FIELD_WARCFILENAME = "Filename";
    public static final String NAMED_FIELD_DESCRIPTION = "Description";
    public static final String NAMED_FIELD_FILEDESC = "ARC-FileDesc";
    public static final String NAMED_FIELD_TRUNCATED = "Truncated";
    public static final String NAMED_FIELD_TRUNCATED_VALUE_TIME = "time";
    public static final String NAMED_FIELD_TRUNCATED_VALUE_LEN = "length";
    public static final String NAMED_FIELD_TRUNCATED_VALUE_HEAD =
        "long-headers";
    public static final String NAMED_FIELD_TRUNCATED_VALUE_UNSPECIFIED = null;
    
    // Headers new to version 0.12 of spec.
    public static final String HEADER_KEY_URI = "WARC-Subject-URI";
    public static final String HEADER_KEY_DATE = "WARC-Date";
    public static final String HEADER_KEY_TYPE = "WARC-Type";
    public static final String HEADER_KEY_IP = "WARC-IP-Address";   
    public static final String HEADER_KEY_CHECKSUM = "WARC-Content-Digest";
    public static final String HEADER_KEY_CONCURRENT_TO =
        "WARC-Concurrent-To";
    public static final String HEADER_KEY_TRUNCATED = "WARC-Truncated";
    public static final String HEADER_KEY_PROFILE = "WARC-Profile";
    public static final String HEADER_KEY_FILENAME = "WARC-Filename";
    public static final String HEADER_KEY_ETAG = "WARC-Etag";
    public static final String HEADER_KEY_LAST_MODIFIED = "WARC-Last-Modified";
    
    public static final String PROFILE_REVISIT_IDENTICAL_DIGEST = 
    	"http://netpreserve.org/warc/0.12/revisit/identical-content-digest";
    public static final String PROFILE_REVISIT_NOT_MODIFIED = 
    	"http://netpreserve.org/warc/0.12/revisit/server-not-modified";
    public static final String PROFILE_CONVERSION_SOFTWARE_COMMAND = 
    	"http://netpreserve.org/warc/0.12/conversion/software-command";
    
    public static final String MIME_VERSION = "MIME-Version: 1.0";
    public static final String CONTENT_ID = "Content-ID";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_DESCRIPTION = "Content-Description";
    
    public static final String COLON_SPACE = ": ";
    // TODO: This is not in spec. Fix.
    public static final String TRUNCATED_VALUE_UNSPECIFIED = "unspecified";
    
    
    /**
     * To be safe, lets use application type rather than message. Regards 
     * 'message/http', RFC says "...provided that it obeys the MIME restrictions
     * for all 'message' types regarding line length and encodings."  This
     * usually means lines of 1000 octets max (unless a 
     * 'Content-Transfer-Encoding: binary' mime header is present).
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.1">rfc2616 section 19.1</a>
     */
    public static final String HTTP_REQUEST_MIMETYPE =
    	"application/http; msgtype=request";
    public static final String HTTP_RESPONSE_MIMETYPE =
    	"application/http; msgtype=response";
}
