/* Copyright (C) 2004 The Trustees of Indiana University. All rights reserved.
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

package edu.indiana.cs.webmining;

/**
 * User: Eran Chinthaka (echintha@cs.indiana.edu)
 * Date: Feb 2, 2007
 */
public abstract class Constants {
    // This will hold all the system wide constants

    // this is to be polite to the others. Let's let the blog web masters know who we are.
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String USER_AGENT_VAL = "Graduate Class Project-CS-b659-Indiana Universirty Bloomington.";

    // The link types
    public static final int LINK_BLOG_TO_BLOG = 111;
    public static final int LINK_BLOG_TO_ENTRY = 222;
    public static final int LINK_ENTRY_TO_ENTRY = 333;

    // Following constants will be used in identifying the blogs
    public static final int NOT_A_BLOG = -1;
    public static final int BLOG = 1;// These are the blogs that we know how to process
    public static final int BLOGGER = 11;
    public static final int BLOGSPOT = 12;
    public static final int BLOGLINES = 13;

    // Frontier table status codes
    public static final String STATUS_TO_BE_FETCHED = "ToBeFetched";
    public static final String STATUS_FETCHING = "Fetching";
    public static final String STATUS_FETCHED = "Fetched";
    public static final String STATUS_BLOG_PROCESSING = "BlogProcessing";
    public static final String STATUS_COMPLETED_PROCESSING = "CompletedProcessing";
    public static final String STATUS_FAILED = "Failed";

    public static final int MAX_THREADS = 20;
    
    public static final int SIM_ALGO_BASIC = 5;
}
