/**
 * Copyright (C) 2007 The Trustees of Indiana University. All rights reserved.
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

package edu.indiana.cs.webmining.db;

import edu.indiana.cs.webmining.util.ResourceUser;
import edu.indiana.cs.webmining.util.Using;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Michel Salim <msalim@cs.indiana.edu>
 * @since Feb 10, 2007
 */
public class DBInitializer {

    private static Connection conn;

    private static void addNode(String url) throws SQLException {
        String queryStr = "INSERT INTO blogs (url) "
                + "VALUES ('" + url + "');";

        Statement stmt = conn.createStatement();
        stmt.execute(queryStr);
        stmt.close();
    }

    private static int getNodeID(final String url) throws SQLException {
        ResourceUser<Statement, Integer, SQLException> user =
                new ResourceUser<Statement, Integer, SQLException>() {

                    public Integer run(Statement stmt) throws SQLException {
                        // TODO Auto-generated method stub
                        String queryStr = "SELECT id FROM blogs "
                                + "WHERE url='" + url + "';";
                        int res;
                        ResultSet results = stmt.executeQuery(queryStr);
                        if (results.first()) {
                            res = results.getInt(1);
                        } else {
                            addNode(url);
                            res = getNodeID(url);
                        }
                        return res;
                    }

                };

        //System.out.println("Finding ID for " + url);
        Statement stmt = conn.createStatement();
        return Using.using(stmt, user);
    }

    private static void addLink(String src, String dest) {
        Statement stmt;
        try {
            final int srcid = getNodeID(src);
            final int destid = getNodeID(dest);
            ResourceUser<Statement, Object, SQLException> user =
                    new ResourceUser<Statement, Object, SQLException>() {

                        public Object run(Statement stmt) throws SQLException {
                            // TODO Auto-generated method stub
                            String queryStr = "INSERT IGNORE INTO links (srcid, destid) "
                                    + "VALUES (" + srcid + ", " + destid + ");";
                            stmt.execute(queryStr);

                            return null;
                        }

                    };
            stmt = conn.createStatement();
            Using.using(stmt, user);
        } catch (SQLException e) {
            System.err.println("Link addition failed:");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Sanitizes a URL by removing within-site link etc.
     *
     * @param url Original URL
     * @return The sanitized URL
     * @throws MalformedURLException
     */
    public static String sanitizeURL(String url) throws MalformedURLException {
        return new URL(url).getHost().toLowerCase();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        BufferedReader reader;

        if (args.length < 1) {
            System.err.println("Usage: java DBInitializer filename");
            return;
        }

        try {
            reader = new BufferedReader(new FileReader(args[0]));

            try {
                conn = DBManager.getConnection();
                //StringTokenizer tokens;
                String[] tokens;
                String src;
                String dest;
                String line = reader.readLine();
                while (line != null) {
                    tokens = line.split("[\\s,]+");
                    try {
                        src = sanitizeURL(tokens[1]);
                        dest = sanitizeURL(tokens[0]);
                        addLink(src, dest);
                    } catch (MalformedURLException e) {
                        System.err.println("Malformed URL: " + e.getMessage());
                    }
//					System.out.println("|" + src + "|" + dest + "|");
                    line = reader.readLine();
                }
                reader.readLine();
                conn.close();
            } catch (SQLException e) {
                System.err.println("Failed to open database connection");
                e.printStackTrace();
            }

            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            return;
        } catch (IOException e) {
            System.err.println("Cannot perform IO operation");
        }
        System.out.println("Database populated");
	}
}
