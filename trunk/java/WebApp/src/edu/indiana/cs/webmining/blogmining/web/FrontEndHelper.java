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

package edu.indiana.cs.webmining.blogmining.web;

import edu.indiana.cs.webmining.analyzer.JungController;
import edu.indiana.cs.webmining.analyzer.util.MCSandbox;
import edu.indiana.cs.webmining.blogmining.web.dto.BlogSearchResult;
import edu.indiana.cs.webmining.db.DBManager;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Mar 23, 2007
 */
public class FrontEndHelper {
    /**
     * When user sends urls, this will be used to validate whether the given links by the user are
     * actually referring to blogs. This will either query our database or dynamically connect to the
     * given link and check whether it is a blog or not, using our BlogDetector.
     * But I don't think there will be meaning doing the second option as we can predict links only
     * to whatever we have in the database.
     *
     * @param url - the link user provides
     * @return whether this is referring to a blog or not.
     */
    public boolean isBlog(String url) {

        return true;
    }

    /**
     * This will search for the similar blogs and will return a set of blogs that are relevant for the
     * given url(s)
     *
     * @param firstURL  - the first blog to match. This should always be not NULL
     * @param secondURL - optional second blog.
     * @return set of relevant blogs, encapsulated in a list of BlogSearchResult objects
     */
    public List<BlogSearchResult> getRelevantBlogs(String firstURL, String secondURL) {
        try {
            List<BlogSearchResult> results = new ArrayList<BlogSearchResult>();
            MCSandbox mcSandbox = new MCSandbox();
            JungController jc = new JungController();
            DBManager dbm = new DBManager();
            DirectedSparseGraph descTree = MCSandbox.getNeighborsGraph(firstURL, jc);

            HashMap<String, Double> foaf = MCSandbox.getFOAF(descTree, jc, firstURL, 1);

            for (String s : foaf.keySet()) {
                results.add(new BlogSearchResult(s, foaf.get(s) + ""));
            }

            return results;
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return new ArrayList<BlogSearchResult>(0);
    }
}
