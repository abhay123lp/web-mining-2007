/* ReplayCharSequenceTest
 *
 * Created on Dec 26, 2006
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
package org.archive.io;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import org.archive.util.TmpDirTestCase;

/**
 * Test ReplayCharSequences.
 *
 * @author stack, gojomo
 * @version $Revision: 4831 $, $Date: 2006-12-27 01:06:07 +0000 (Wed, 27 Dec 2006) $
 */
public class ReplayCharSequenceTest extends TmpDirTestCase
{
    /**
     * Logger.
     */
    private static Logger logger =
        Logger.getLogger("org.archive.io.ReplayCharSequenceFactoryTest");


    private static final int SEQUENCE_LENGTH = 127;
    private static final int MULTIPLIER = 3;
    private static final int BUFFER_SIZE = SEQUENCE_LENGTH * MULTIPLIER;
    private static final int INCREMENT = 1;

    /**
     * Buffer of regular content.
     */
    private byte [] regularBuffer = null;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        this.regularBuffer =
            fillBufferWithRegularContent(new byte [BUFFER_SIZE]);
    }
    
    public void testShiftjis() throws IOException {

        // Here's the bytes for the JIS encoding of the Japanese form of Nihongo
        byte[] bytes_nihongo = {
            (byte) 0x1B, (byte) 0x24, (byte) 0x42, (byte) 0x46,
            (byte) 0x7C, (byte) 0x4B, (byte) 0x5C, (byte) 0x38,
            (byte) 0x6C, (byte) 0x1B, (byte) 0x28, (byte) 0x42,
            (byte) 0x1B, (byte) 0x28, (byte) 0x42 };
        final String ENCODING = "SJIS";
        // Here is nihongo converted to JVM encoding.
        String nihongo = new String(bytes_nihongo, ENCODING);

        RecordingOutputStream ros = writeTestStream(
                bytes_nihongo,MULTIPLIER,
                "testShiftjis",MULTIPLIER);
        // TODO: check for existence of overflow file?
        ReplayCharSequence rcs = ros.getReplayCharSequence(ENCODING);
            
        // Now check that start of the rcs comes back in as nihongo string.
        String rcsStr = rcs.subSequence(0, nihongo.length()).toString();
        assertTrue("Nihongo " + nihongo + " does not equal converted string" +
                " from rcs " + rcsStr,
            nihongo.equals(rcsStr));
        // And assert next string is also properly nihongo.
        if (rcs.length() >= (nihongo.length() * 2)) {
            rcsStr = rcs.subSequence(nihongo.length(),
                nihongo.length() + nihongo.length()).toString();
            assertTrue("Nihongo " + nihongo + " does not equal converted " +
                " string from rcs (2nd time)" + rcsStr,
                nihongo.equals(rcsStr));
        }
    }

    public void testGetReplayCharSequenceByteZeroOffset() throws IOException {

        RecordingOutputStream ros = writeTestStream(
                regularBuffer,MULTIPLIER,
                "testGetReplayCharSequenceByteZeroOffset",MULTIPLIER);
        ReplayCharSequence rcs = ros.getReplayCharSequence();

        for (int i = 0; i < MULTIPLIER; i++) {
            accessingCharacters(rcs);
        }
    }

    public void testGetReplayCharSequenceByteOffset() throws IOException {

        RecordingOutputStream ros = writeTestStream(
                regularBuffer,MULTIPLIER,
                "testGetReplayCharSequenceByteOffset",MULTIPLIER);
        ReplayCharSequence rcs = ros.getReplayCharSequence(null,SEQUENCE_LENGTH);

        for (int i = 0; i < MULTIPLIER; i++) {
            accessingCharacters(rcs);
        }
    }

    public void testGetReplayCharSequenceMultiByteZeroOffset()
        throws IOException {

        RecordingOutputStream ros = writeTestStream(
                regularBuffer,MULTIPLIER,
                "testGetReplayCharSequenceMultiByteZeroOffset",MULTIPLIER);
        ReplayCharSequence rcs = ros.getReplayCharSequence("UTF-8");

        for (int i = 0; i < MULTIPLIER; i++) {
            accessingCharacters(rcs);
        }
    }

    public void testGetReplayCharSequenceMultiByteOffset() throws IOException {

        RecordingOutputStream ros = writeTestStream(
                regularBuffer,MULTIPLIER,
                "testGetReplayCharSequenceMultiByteOffset",MULTIPLIER);
        ReplayCharSequence rcs = ros.getReplayCharSequence("UTF-8", SEQUENCE_LENGTH);

        try {
            for (int i = 0; i < MULTIPLIER; i++) {
                accessingCharacters(rcs);
            }
        } finally {
            rcs.close();
        }
    }
    
    public void testReplayCharSequenceByteToString() throws IOException {
        String fileContent = "Some file content";
        byte [] buffer = fileContent.getBytes();
        RecordingOutputStream ros = writeTestStream(
                buffer,1,
                "testReplayCharSequenceByteToString.txt",0);
        ReplayCharSequence rcs = ros.getReplayCharSequence();
        String result = rcs.toString();
        assertEquals("Strings don't match",result,fileContent);
    }
    
    public void testReplayCharSequenceByteToStringMulti() throws IOException {
        String fileContent = "Some file content";
        byte [] buffer = fileContent.getBytes("UTF-8");
        final int MULTIPLICAND = 10;
        StringBuilder sb =
            new StringBuilder(MULTIPLICAND * fileContent.length());
        for (int i = 0; i < MULTIPLICAND; i++) {
            sb.append(fileContent);
        }
        String expectedResult = sb.toString();
        RecordingOutputStream ros = writeTestStream(
                buffer,1,
                "testReplayCharSequenceByteToStringMulti.txt",MULTIPLICAND-1);
        for (int i = 0; i < 3; i++) {
            ReplayCharSequence rcs = ros.getReplayCharSequence("UTF-8");
            String result = rcs.toString();
            assertEquals("Strings don't match", result, expectedResult);
            rcs.close();
        }
    }
    
    /**
     * Accessing characters test.
     *
     * Checks that characters in the rcs are in sequence.
     *
     * @param rcs The ReplayCharSequence to try out.
     */
    private void accessingCharacters(CharSequence rcs) {
        long timestamp = (new Date()).getTime();
        int seeks = 0;
        for (int i = (INCREMENT * 2); (i + INCREMENT) < rcs.length();
                i += INCREMENT) {
            checkCharacter(rcs, i);
            seeks++;
            for (int j = i - INCREMENT; j < i; j++) {
                checkCharacter(rcs, j);
                seeks++;
            }
        }
        // Note that printing out below breaks cruisecontrols drawing
        // of the xml unit test results because it outputs disallowed
        // xml characters.
        logger.fine(rcs + " seeks count " + seeks + " in " +
            ((new Date().getTime()) - timestamp) + " milliseconds.");
    }

    /**
     * Check the character read.
     *
     * Throws assertion if not expected result.
     *
     * @param rcs ReplayCharSequence to read from.
     * @param i Character offset.
     */
    private void checkCharacter(CharSequence rcs, int i) {
        int c = rcs.charAt(i);
        assertTrue("Character " + Integer.toString(c) + " at offset " + i +
            " unexpected.", (c % SEQUENCE_LENGTH) == (i % SEQUENCE_LENGTH));
    }

    /**
     * @param baseName
     * @return RecordingOutputStream
     * @throws IOException
     */
    private RecordingOutputStream writeTestStream(byte[] content, 
            int memReps, String baseName, int fileReps) throws IOException {
        RecordingOutputStream ros = new RecordingOutputStream(
                content.length * memReps,
                baseName);
        ros.open();
        for(int i = 0; i < (memReps+fileReps); i++) {
            // fill buffer (repeat MULTIPLIER times) and 
            // overflow to disk (also MULTIPLIER times)
            ros.write(content);
        }
        ros.close();
        return ros; 
    }


    /**
     * Fill a buffer w/ regular progression of single-byte 
     * (and <= 127) characters.
     * @param buffer Buffer to fill.
     * @return The buffer we filled.
     */
    private byte [] fillBufferWithRegularContent(byte [] buffer) {
        int index = 0;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) (index & 0x00ff);
            index++;
            if (index >= SEQUENCE_LENGTH) {
                // Reset the index.
                index = 0;
            }
        }
        return buffer;
    }

    public void testCheckParameters()
    {
        // TODO.
    }
}
