/*
 * Heritrix
 *
 * $Id: ExtractorSWF.java 4653 2006-09-25 18:58:50Z paul_jack $
 *
 * Created on March 19, 2004
 *
 * Copyright (C) 2003 Internet Archive.
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

package org.archive.crawler.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;

import com.anotherbigidea.flash.interfaces.SWFTagTypes;
import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.readers.TagParser;
import com.anotherbigidea.io.InStream;

/**
 * Extracts URIs from SWF (flash/shockwave) files.
 * 
 * To test, here is a link to an swf that has links
 * embedded inside of it: http://www.hitspring.com/index.swf.
 *
 * @author Igor Ranitovic
 */
public class ExtractorSWF
extends Extractor
implements CoreAttributeConstants {

    private static final long serialVersionUID = 3627359592408010589L;

    private static Logger logger =
        Logger.getLogger(ExtractorSWF.class.getName());
    protected long numberOfCURIsHandled = 0;
    protected long numberOfLinksExtracted = 0;
    // TODO: consider if this should be even smaller, because anything 
    // containing URLs wouldn't be this big
    private static final int MAX_READ_SIZE = 1024 * 1024; // 1MB

    /**
     * @param name
     */
    public ExtractorSWF(String name) {
        super(name, "Flash extractor. Extracts URIs from SWF " +
            "(flash/shockwave) files.");
    }

    protected void extract(CrawlURI curi) {
        if (!isHttpTransactionContentToProcess(curi)) {
            return;
        }

        String contentType = curi.getContentType();
        if (contentType == null) {
            return;
        }
        if ((contentType.toLowerCase().indexOf("x-shockwave-flash") < 0)
                && (!curi.toString().toLowerCase().endsWith(".swf"))) {
            return;
        }

        numberOfCURIsHandled++;

        InputStream documentStream = null;
        // Get the SWF file's content stream.
        try {
            documentStream = curi.getHttpRecorder().getRecordedInput().
                getContentReplayInputStream();
            if (documentStream == null) {
                return;
            }

            // Create SWF action that will add discoved URIs to CrawlURI
            // alist(s).
            CrawlUriSWFAction curiAction = new CrawlUriSWFAction(curi,
                    getController());
            // Overwrite parsing of specific tags that might have URIs.
            CustomSWFTags customTags = new CustomSWFTags(curiAction);
            // Get a SWFReader instance.
            SWFReader reader =
                new SWFReader(getTagParser(customTags), documentStream) {
                /**
                 * Override because a corrupt SWF file can cause us to try
                 * read lengths that are hundreds of megabytes in size
                 * causing us to OOME.
                 * 
                 * Below is copied from SWFReader parent class.
                 */
                public int readOneTag() throws IOException {
                    int header = mIn.readUI16();
                    int  type   = header >> 6;    //only want the top 10 bits
                    int  length = header & 0x3F;  //only want the bottom 6 bits
                    boolean longTag = (length == 0x3F);
                    if(longTag) {
                        length = (int)mIn.readUI32();
                    }
                    // Below test added for Heritrix use.
                    if (length > MAX_READ_SIZE) {
                        // skip to next, rather than throw IOException ending
                        // processing
                        mIn.skipBytes(length);
                        logger.info("oversized SWF tag (type=" + type
                                + ";length=" + length + ") skipped");
                    } else {
                        byte[] contents = mIn.read(length);
                        mConsumer.tag(type, longTag, contents);
                    }
                    return type;
                }
            };
            
            reader.readFile();
            numberOfLinksExtracted += curiAction.getLinkCount();
        } catch (IOException e) {
            curi.addLocalizedError(getName(), e, "Fail reading.");
        } finally {
            try {
                documentStream.close();
            } catch (IOException e) {
                curi.addLocalizedError(getName(), e, "Fail on close.");
            }
        }

        // Set flag to indicate that link extraction is completed.
        curi.linkExtractorFinished();
        logger.fine(curi + " has " + numberOfLinksExtracted + " links.");
    }
    
    public String report() {
        StringBuffer ret = new StringBuffer();
        ret.append("Processor: org.archive.crawler.extractor.ExtractorSWF\n");
        ret.append("  Function:          Link extraction on Shockwave Flash " +
            "documents (.swf)\n");

        ret.append("  CrawlURIs handled: " + numberOfCURIsHandled + "\n");
        ret.append("  Links extracted:   " + numberOfLinksExtracted + "\n\n");
        return ret.toString();
    }
    
    
    /**
     * Get a TagParser
     * 
     * A custom ExtractorTagParser which ignores all the big binary image/
     * sound/font types which don't carry URLs is used, to avoid the 
     * occasionally fatal (OutOfMemoryError) memory bloat caused by the
     * all-in-memory SWF library handling. 
     * 
     * @param customTags A custom tag parser.
     * @return An SWFReader.
     */
    private TagParser getTagParser(CustomSWFTags customTags) {
        return new ExtractorTagParser(customTags);
    }
    
    /**
     * TagParser customized to ignore SWFTags that 
     * will never contain extractable URIs. 
     */
    protected class ExtractorTagParser extends TagParser {

        protected ExtractorTagParser(SWFTagTypes tagtypes) {
            super(tagtypes);
        }

        protected void parseDefineBits(InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in bits
        }

        protected void parseDefineBitsJPEG3(InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in bits
        }

        protected void parseDefineBitsLossless(InStream in, int length, boolean hasAlpha) throws IOException {
            // DO NOTHING - no URLs to be found in bits
        }

        protected void parseDefineButtonSound(InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in sound
        }

        protected void parseDefineFont(InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in font
        }

        protected void parseDefineJPEG2(InStream in, int length) throws IOException {
            // DO NOTHING - no URLs to be found in jpeg
        }

        protected void parseDefineJPEGTables(InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in jpeg
        }

        protected void parseDefineShape(int type, InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in shape
        }

        protected void parseDefineSound(InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in sound
        }

        protected void parseFontInfo(InStream in, int length, boolean isFI2) throws IOException {
            // DO NOTHING - no URLs to be found in font info
        }

        protected void parseDefineFont2(InStream in) throws IOException {
            // DO NOTHING - no URLs to be found in bits
        }
    }
}
