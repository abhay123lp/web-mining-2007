/* Copyright (C) 2007 The Trustees of Indiana University. All rights reserved.
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

package edu.indiana.cs.webmining.bean;

import edu.indiana.cs.webmining.util.HashManager;

/**
 * @author Eran Chinthaka (echintha@cs.indiana.edu)
 * @author Michel Salim (msalim@cs.indiana.edu)
 * @since  Feb 11, 2007
 */
public class Blog {
    private int id;
    private String url;
    private long[] urlHash;

    public Blog(int id, String url) {
        this.id = id;
        this.url = url;
        this.urlHash = HashManager.hash(url);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of a Blog object. Also updates the URL hash
     * 
     * @param url The new URL
     */
    public void setUrl(String url) {
        this.url = url;
        setUrlHash(HashManager.hash(url));
    }

    public long[] getUrlHash() {
        return urlHash;
    }

    public void setUrlHash(long[] urlHash) {
        this.urlHash = urlHash;
    }

    // Not sure if this should be overridden?
    public boolean equals(Object o) {
        return ((o.getClass() == this.getClass()) && equals((Blog) o));
    }

    /**
     * Two Blog objects are equal iff their URLs hash to the same value and
     * (in case of collision) their URLs are the same
     * 
     * @param that The other blog
     * @return <b>true</b> if the two blogs have the same URL, <b>false</b> otherwise
     */
    public boolean equals(Blog that) {
        long[] hash1 = this.getUrlHash();
        long[] hash2 = that.getUrlHash();
        
        return ((hash1[0] == hash2[0]) && (hash1[1] == hash2[1]) 
                && (this.getUrl().compareTo(that.getUrl())==0));
    }
}