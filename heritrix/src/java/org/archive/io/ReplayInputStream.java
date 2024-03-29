/* ReplayInputStream
 *
 * $Id: ReplayInputStream.java 4897 2007-02-13 21:35:16Z stack-sf $
 *
 * Created on Sep 24, 2003
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
package org.archive.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Replays the bytes recorded from a RecordingInputStream or
 * RecordingOutputStream.
 *
 * This InputStream supports mark and reset.
 *
 * @author gojomo
 */
public class ReplayInputStream extends SeekInputStream
{
    private BufferedSeekInputStream diskStream;
    private byte[] buffer;
    private long position;

    /**
     * Total size of stream content.
     *
     * Size of data to replay.
     */
    private long size = -1;

    /**
     * Where the response body starts, if marked
     */
    protected long responseBodyStart = -1;


    /**
     * Constructor.
     *
     * @param buffer Buffer to read from.
     * @param size Size of data to replay.
     * @param responseBodyStart Start of the response body.
     * @param backingFilename Backing file that sits behind the buffer.  If
     * <code>size<code> > than buffer then we go to backing file to read
     * data that is beyond buffer.length.
     *
     * @throws IOException If we fail to open an input stream on
     * backing file.
     */
    public ReplayInputStream(byte[] buffer, long size, long responseBodyStart,
            String backingFilename)
        throws IOException
    {
        this(buffer, size, backingFilename);
        this.responseBodyStart = responseBodyStart;
    }

    /**
     * Constructor.
     *
     * @param buffer Buffer to read from.
     * @param size Size of data to replay.
     * @param backingFilename Backing file that sits behind the buffer.  If
     * <code>size<code> > than buffer then we go to backing file to read
     * data that is beyond buffer.length.
     * @throws IOException If we fail to open an input stream on
     * backing file.
     */
    public ReplayInputStream(byte[] buffer, long size, String backingFilename)
        throws IOException
    {
        this.buffer = buffer;
        this.size = size;
        if (size > buffer.length) {
            RandomAccessInputStream rais = new RandomAccessInputStream(
                    new File(backingFilename));
            diskStream = new BufferedSeekInputStream(rais, 4096);
        }
    }

    public long setToResponseBodyStart() throws IOException {
        position(responseBodyStart);
        return this.position;
    }
    

    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (position == size) {
            return -1; // EOF
        }
        if (position < buffer.length) {
            // Convert to unsigned int.
            int c = buffer[(int) position] & 0xFF;
            position++;
            return c;
        }
        int c = diskStream.read();
        if (c >= 0) {
            position++;
        }
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (position == size) {
            return -1; // EOF
        }
        if (position < buffer.length) {
            int toCopy = (int)Math.min(size - position,
                Math.min(len, buffer.length - position));
            System.arraycopy(buffer, (int)position, b, off, toCopy);
            if (toCopy > 0) {
                position += toCopy;
            }
            return toCopy;
        }
        // into disk zone
        int read = diskStream.read(b,off,len);
        if(read>0) {
            position += read;
        }
        return read;
    }

    public void readFullyTo(OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int c = read(buf);
        while (c != -1) {
            os.write(buf,0,c);
            c = read(buf);
        }
    }
    
    /*
     * Like 'readFullyTo', but only reads the header-part.
     * Starts from the beginning each time it is called.
     */
    public void readHeaderTo(OutputStream os) throws IOException {
        position = 0;
        byte[] buf = new byte[(int)responseBodyStart];
        int c = read(buf,0,buf.length);
        if(c != -1) {
            os.write(buf,0,c);
        }
    }

    /*
     * Like 'readFullyTo', but only reads the content-part.
     */
    public void readContentTo(OutputStream os) throws IOException {
        setToResponseBodyStart();
        byte[] buf = new byte[4096];
        int c = read(buf);
        while (c != -1) {
            os.write(buf,0,c);
            c = read(buf);            
        }
    }
    
    public void readContentTo(OutputStream os, int maxSize) throws IOException {
        setToResponseBodyStart();
        byte[] buf = new byte[4096];
        int c = read(buf);
        int tot = 0;
        while (c != -1 && tot < maxSize) {
            os.write(buf,0,c);
            c = read(buf);
            tot += c;
        }
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        super.close();
        if(diskStream != null) {
            diskStream.close();
        }
    }

    /**
     * Total size of stream content.
     * @return Returns the size.
     */
    public long getSize()
    {
        return size;
    }
    
    /**
     * Total size of header.
     * @return the size of the header.
     */
    public long getHeaderSize()
    {
        return responseBodyStart;
    }
    
    /**
     * Total size of content.
     * @return the size of the content.
     */
    public long getContentSize()
    {
        return size - responseBodyStart;
    }

    /**
     * @return Amount THEORETICALLY remaining (TODO: Its not theoretical
     * seemingly.  The class implemetentation depends on it being exact).
     */
    public long remaining() {
        return size - position;
    }
    

    /**
     * Reposition the stream.
     * 
     * @param p  the new position for this stream
     * @throws IOException  if an IO error occurs
     */
    public void position(long p) throws IOException {
        if (p < 0) {
            throw new IOException("Negative seek offset.");
        }
        if (p >= size) {
            throw new IOException("Desired position exceeds size.");
        }
        if (p < buffer.length) {
            // Only seek file if necessary
            if (position > buffer.length) {
                diskStream.position(0);
            }
        } else {
            diskStream.position(p - buffer.length);
        }
        this.position = p;
    }
    
    
    public long position() throws IOException {
        return position;
    }
}
