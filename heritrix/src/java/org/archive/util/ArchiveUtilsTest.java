/* ArchiveUtilsTest
 *
 * $Id: ArchiveUtilsTest.java 4877 2007-01-24 00:34:36Z gojomo $
 *
 * Created Tue Jan 20 14:17:59 PST 2004
 *
 * Copyright (C) 2004 Internet Archive.
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

package org.archive.util;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.ParseException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test suite for ArchiveUtils
 *
 * @author <a href="mailto:me@jamesc.net">James Casey</a>
 * @version $Id: ArchiveUtilsTest.java 4877 2007-01-24 00:34:36Z gojomo $
 */
public class ArchiveUtilsTest extends TestCase {
    
    /**
     * Create a new ArchiveUtilsTest object
     *
     * @param testName the name of the test
     */
    public ArchiveUtilsTest(final String testName) {
        super(testName);
    }

    /**
     * run all the tests for ArchiveUtilsTest
     *
     * @param argv the command line arguments
     */
    public static void main(String argv[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * return the suite of tests for ArchiveUtilsTest
     *
     * @return the suite of test
     */
    public static Test suite() {
        return new TestSuite(ArchiveUtilsTest.class);
    }

    /** check the getXXDigitDate() methods produce valid dates*/
    public void testGetXXDigitDate() {
        // TODO - we only really test the date lengths here.  How to test
        // other stuff well ?
        final String date12 = ArchiveUtils.get12DigitDate();
        assertEquals("12 digits", 12, date12.length());

        final String date14 = ArchiveUtils.get14DigitDate();
        assertEquals("14 digits", 14, date14.length());

        final String date17 = ArchiveUtils.get17DigitDate();
        assertEquals("17 digits", 17, date17.length());

        // now parse, and check they're all within 1 minute

        try {
            final long long12 = ArchiveUtils.parse12DigitDate(date12).getTime();
            long long14 = ArchiveUtils.parse14DigitDate(date14).getTime();
            long long17 = ArchiveUtils.parse17DigitDate(date17).getTime();

            assertClose("12 and 14 close", long12, long14, 600000);
            assertClose("12 and 17 close", long12, long17, 600000);
            assertClose("14 and 17 close", long14, long17, 600000);
        } catch (ParseException e) {
            fail("Could not parse a date : " + e.getMessage());
        }
    }

    /** check that getXXDigitDate(long) does the right thing */
    public void testGetXXDigitDateLong() {
        final long now = System.currentTimeMillis();
        final String date12 = ArchiveUtils.get12DigitDate(now);
        assertEquals("12 digits", 12, date12.length());

        final String date14 = ArchiveUtils.get14DigitDate(now);
        assertEquals("14 digits", 14, date14.length());
        assertEquals("first twelve digits same as date12", date12, date14.substring(0, 12));
        final String date17 = ArchiveUtils.get17DigitDate(now);
        assertEquals("17 digits", 17, date17.length());
        assertEquals("first twelve digits same as date12", date12, date17.substring(0, 12));
        assertEquals("first fourteen digits same as date14", date14, date17.substring(0, 14));
    }

    /**
     * Check that parseXXDigitDate() works
     *
     * @throws ParseException
     */
    public void testParseXXDigitDate() throws ParseException {
        // given a date, check it get resolved properly
        // It's 02 Jan 2004, 12:40:02.111
        final String date = "20040102124002111";
        try {
            final long long12 = ArchiveUtils.parse12DigitDate(date.substring(0, 12)).getTime();
            final long long14 = ArchiveUtils.parse14DigitDate(date.substring(0, 14)).getTime();
            final long long17 = ArchiveUtils.parse17DigitDate(date).getTime();

            assertClose("12 and 14 close", long12, long14, 600000);
            assertClose("12 and 17 close", long12, long17, 600000);
            assertClose("14 and 17 close", long14, long17, 600000);
        } catch (ParseException e) {
            fail("Could not parse a date : " + e.getMessage());
        }
    }
    
    public void testTooShortParseDigitDate() throws ParseException {
        String d = "X";
        boolean b = false;
        try {
            ArchiveUtils.getDate(d);
        } catch (ParseException e) {
            b = true;
        }
        assertTrue(b);
        
        Date date = ArchiveUtils.getDate("1999");
        assertTrue(date.getTime() == 915148800000L);
        
        b = false;
        try {
            ArchiveUtils.getDate("19991");
        } catch (ParseException e) {
            b = true;
        }
        assertTrue(b);
        
        ArchiveUtils.getDate("19990101");
        ArchiveUtils.getDate("1999010101");
        ArchiveUtils.getDate("19990101010101"); 
        ArchiveUtils.getDate("1960"); 
    }

    /** check that parse12DigitDate doesn't accept a bad date */
    public void testBad12Date() {
        // now try a badly formed dates
        assertBad12DigitDate("a-stringy-digit-date");
        assertBad12DigitDate("20031201"); // too short
    }

    /**
     * check that parse14DigitDate doesn't accept a bad date
     */
    public void testBad14Date() {
        // now try a badly formed dates
        assertBad14DigitDate("a-stringy-digit-date");
        assertBad14DigitDate("20031201"); // too short
        assertBad14DigitDate("200401021240");  // 12 digit
    }
    /**
     * check that parse12DigitDate doesn't accept a bad date
     */
    public void testBad17Date() {
        // now try a badly formed dates
        assertBad17DigitDate("a-stringy-digit-date");
        assertBad17DigitDate("20031201"); // too short
        assertBad17DigitDate("200401021240");  // 12 digit
        assertBad17DigitDate("20040102124002");  // 14 digit
    }

    /** check that padTo(String) works */
    public void testPadToString() {
        assertEquals("pad to one (smaller)", "foo", ArchiveUtils.padTo("foo", 1));
        assertEquals("pad to 0 (no sense)", "foo", ArchiveUtils.padTo("foo", 0));
        assertEquals("pad to neg (nonsense)", "foo", ArchiveUtils.padTo("foo", 0));
        assertEquals("pad to 4", " foo", ArchiveUtils.padTo("foo", 4));
        assertEquals("pad to 10", "       foo", ArchiveUtils.padTo("foo", 10));
    }

    /**
     * check that padTo(int) works
     */
    public void testPadToInt() {
        assertEquals("pad to one (smaller)", "123", ArchiveUtils.padTo(123, 1));
        assertEquals("pad to 0 (no sense)", "123", ArchiveUtils.padTo(123, 0));
        assertEquals("pad to neg (nonsense)", "123", ArchiveUtils.padTo(123, 0));
        assertEquals("pad to 4", " 123", ArchiveUtils.padTo(123, 4));
        assertEquals("pad to 10", "       123", ArchiveUtils.padTo(123, 10));
        assertEquals("pad -123 to 10", "      -123", ArchiveUtils.padTo(-123, 10));
    }

    /** check that byteArrayEquals() works */
    public void testByteArrayEquals() {
        // foo == foo2, foo != bar, foo != bar2
        byte[] foo = new byte[10], bar = new byte[20];
        byte[] foo2 = new byte[10], bar2 = new byte[10];

        for (byte i = 0; i < 10 ; ++i) {
            foo[i] = foo2[i] = bar[i] = i;
            bar2[i] = (byte)(01 + i);
        }
        assertTrue("two nulls", ArchiveUtils.byteArrayEquals(null, null));
        assertFalse("lhs null", ArchiveUtils.byteArrayEquals(null, foo));
        assertFalse("rhs null", ArchiveUtils.byteArrayEquals(foo, null));

        // now check with same length, with same (foo2) and different (bar2)
        // contents
        assertFalse("different lengths", ArchiveUtils.byteArrayEquals(foo, bar));

        assertTrue("same to itself", ArchiveUtils.byteArrayEquals(foo, foo));
        assertTrue("same contents", ArchiveUtils.byteArrayEquals(foo, foo2));
        assertFalse("different contents", ArchiveUtils.byteArrayEquals(foo, bar2));
    }

    /** test doubleToString() */
    public void testDoubleToString(){
        double test = 12.345;
        assertTrue(
            "cecking zero precision",
            ArchiveUtils.doubleToString(test, 0).equals("12"));
        assertTrue(
            "cecking 2 character precision",
            ArchiveUtils.doubleToString(test, 2).equals("12.34"));
        assertTrue(
            "cecking precision higher then the double has",
            ArchiveUtils.doubleToString(test, 65).equals("12.345"));
    }

    public void testFormatBytesForDisplay(){
        long kb = 1024;
        long mb = 1024*1024*2;
        long gb = ((long)1024*1024)*1024*4;

        assertEquals("formating negative number","0 B",ArchiveUtils.formatBytesForDisplay(-1));
        assertEquals("formating byte - lower bound","0 B",ArchiveUtils.formatBytesForDisplay(0));
        assertEquals("formating byte - upper bound","1023 B",ArchiveUtils.formatBytesForDisplay(kb-1));
        assertEquals("formating kilobyte - lower bound","1 KB",ArchiveUtils.formatBytesForDisplay(kb));
        assertEquals("formating kilobyte - upper bound","2047 KB",ArchiveUtils.formatBytesForDisplay(mb-1));
        assertEquals("formating megabyte - lower bound","2 MB",ArchiveUtils.formatBytesForDisplay(mb));
        assertEquals("formating megabyte - upper bound","4095 MB",ArchiveUtils.formatBytesForDisplay(gb-1));
        assertEquals("formating gigabyte - lower bound","4 GB",ArchiveUtils.formatBytesForDisplay(gb));
    }

    /*
     * helper methods
     */

    /** check that this is a bad date, and <code>fail()</code> if so.
     *
     * @param date the 12digit date to check
     */
    private void assertBad12DigitDate(final String date) {
        try {
            ArchiveUtils.parse12DigitDate(date);
        } catch (ParseException e) {
            return;
        }
        fail("Expected exception on parse of : " + date);

    }
    /**
     * check that this is a bad date, and <code>fail()</code> if so.
     *
     * @param date the 14digit date to check
     */
    private void assertBad14DigitDate(final String date) {
        try {
            ArchiveUtils.parse14DigitDate(date);
        } catch (ParseException e) {
            return;
        }
        fail("Expected exception on parse of : " + date);

    }

    /**
     * check that this is a bad date, and <code>fail()</code> if so.
     *
     * @param date the 17digit date to check
     */
    private void assertBad17DigitDate(final String date) {
        try {
            ArchiveUtils.parse17DigitDate(date);
        } catch (ParseException e) {
            return;
        }
        fail("Expected exception on parse of : " + date);

    }

    /** check that two longs are within a given <code>delta</code> */
    private void assertClose(String desc, long date1, long date2, long delta) {
        assertTrue(desc, date1 == date2 ||
                    (date1 < date2 && date2 < (date1 + delta)) ||
                    (date2 < date1 && date1 < (date2 + delta)));
    }
    
    public void testArrayToLong() {
        testOneArrayToLong(-1);
        testOneArrayToLong(1);
        testOneArrayToLong(1000);
        testOneArrayToLong(Integer.MAX_VALUE);
    }
    
    private void testOneArrayToLong(final long testValue) {
        byte [] a = new byte[8];
        ArchiveUtils.longIntoByteArray(testValue, a, 0);
        final long l = ArchiveUtils.byteArrayIntoLong(a, 0);
        assertEquals(testValue, l);
    }
    
    public void testSecondsSinceEpochCalculation() throws ParseException {
        assertEquals(ArchiveUtils.secondsSinceEpoch("20010909014640"),
            "1000000000");
        assertEquals(ArchiveUtils.secondsSinceEpoch("20010909014639"),
            "0999999999");
        assertEquals(ArchiveUtils.secondsSinceEpoch("19700101"),
            "0000000000");
        assertEquals(ArchiveUtils.secondsSinceEpoch("2005"), "1104537600");
        assertEquals(ArchiveUtils.secondsSinceEpoch("200501"), "1104537600");
        assertEquals(ArchiveUtils.secondsSinceEpoch("20050101"), "1104537600");
        assertEquals(ArchiveUtils.secondsSinceEpoch("2005010100"),
            "1104537600");
        boolean eThrown = false;
        try {
            ArchiveUtils.secondsSinceEpoch("20050");
        } catch (IllegalArgumentException e) {
            eThrown = true;
        }
        assertTrue(eThrown);
    }
    
    public static void testZeroPadInteger() {
        assertEquals(ArchiveUtils.zeroPadInteger(1), "0000000001");
        assertEquals(ArchiveUtils.zeroPadInteger(1000000000), "1000000000");
    }
    
    /**
     * Test stable behavior of date formatting under heavy concurrency. 
     * 
     * @throws InterruptedException
     */
    public static void testDateFormatConcurrency() throws InterruptedException {        
        final int COUNT = 1000;
        Thread [] ts = new Thread[COUNT];
        final Semaphore allDone = new Semaphore(-COUNT+1);
        final AtomicInteger failures = new AtomicInteger(0); 
        for (int i = 0; i < COUNT; i++) {
            Thread t = new Thread() {
                public void run() {
                    long n = System.currentTimeMillis();
                    final String d = ArchiveUtils.get17DigitDate(n);
                    for (int i = 0; i < 1000; i++) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        String d2 = ArchiveUtils.get17DigitDate(n);
                        if(!d.equals(d2)) {
                            failures.incrementAndGet();
                            break; 
                        }
                    }
                    allDone.release();
                }
            };
            ts[i] = t;
            ts[i].setName(Integer.toString(i));
            ts[i].start();
            while(!ts[i].isAlive()) /* Wait for thread to spin up*/;
        }
        allDone.acquire(); // wait for all threads to finish
        assertEquals(failures.get()+" format mismatches",0,failures.get()); 
    }
}

