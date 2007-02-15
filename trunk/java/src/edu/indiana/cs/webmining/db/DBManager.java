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

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import edu.indiana.cs.webmining.bean.Blog;
import edu.indiana.cs.webmining.bean.Link;
import edu.indiana.cs.webmining.util.ResourceUser;
import edu.indiana.cs.webmining.util.Using;

/**
 * 
 * @author Michel Salim <msalim@cs.indiana.edu>
 * @since  Feb 10, 2007
 *
 */
public class DBManager {

	private static Connection conn;
    private static PreparedStatement stmtGetAllBlogs;
    private static PreparedStatement stmtGetAllLinks;

    
    private static void initialize() throws SQLException {
        try {
            // Load MySQL connector
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            FileInputStream propstream = new FileInputStream("etc/login.prop");
            Properties prop = new Properties();
            prop.load(propstream);
            propstream.close();

            String connStr = "jdbc:mysql://localhost/blogmining?user="
                + prop.getProperty("user")
                + "&password="
                + prop.getProperty("password");
            System.out.println("connStr=" + connStr);
            conn = DriverManager.getConnection(connStr);
        } catch (Exception e) {
            throw new SQLException("Connection unavailable");
        }
    }

    /**
     * Returns a Connection
     * @return Connection
     * @throws SQLException
     */
	public static Connection getConnection() throws SQLException {
		if (conn == null) {
            initialize();
		}
		return conn;
	}

	private static Iterable<String> getResults(final ResultSet rs, final int coln) {
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					private String next;
					public boolean hasNext() {
						if (next == null) {
							try {
								if (! rs.next()) {
									return false;
								}
								next = rs.getString(coln);
							} catch (SQLException e) {
								System.err.println("Something wrong in ResultSet");
								return false;
							}
						}
						return true;
					}
					public String next() {
						if (! hasNext()) {
							throw new NoSuchElementException();
						}
						String retval = next;
						next = null;
						return retval;
					}

					public void remove() {
						throw new UnsupportedOperationException("Remove not allowed");
					}
				};

			}
		};
	}
	public static ArrayList<String> getSuccessors(final String url) throws SQLException {
		Statement stmt = getConnection().createStatement();
        ResourceUser<Statement, ArrayList<String>, SQLException> user =
            new ResourceUser<Statement, ArrayList<String>, SQLException>() {

                public ArrayList<String> run(Statement stmt) throws SQLException {
                    ArrayList<String> res = new ArrayList<String>();
                    String connStr = "SELECT url FROM links AS l JOIN blogs AS b "
                        + "ON l.destid = b.id "
                        + "WHERE srcid = (SELECT id FROM blogs "
                        + "WHERE url='" + url + "');";
                    ResultSet rs = stmt.executeQuery(connStr);
                    for (String s_url: getResults(rs, 1)) {
                        res.add(s_url);
                    }
   
                    return res;
                }
            
        };
        return Using.using(stmt, user);
	}
	public static ArrayList<String> getPredecessors(final String url) throws SQLException {
		Statement stmt = getConnection().createStatement();
        ResourceUser<Statement, ArrayList<String>, SQLException> user =
            new ResourceUser<Statement, ArrayList<String>, SQLException>() {

                public ArrayList<String> run(Statement stmt) throws SQLException {
                    ArrayList<String> res = new ArrayList<String>();
                    String connStr = "SELECT url FROM links AS l JOIN blogs AS b "
                        + "ON l.srcid = b.id "
                        + "WHERE destid = (SELECT id FROM blogs "
                        + "WHERE url='" + url + "');";
                    ResultSet rs = stmt.executeQuery(connStr);
                    for (String s_url: getResults(rs, 1)) {
                        res.add(s_url);
                    }
                    return res;
                }
            
        };
		return Using.using(stmt, user);
	}
	public static ArrayList<String> getNeighbors(String url) throws SQLException {
		ArrayList<String> res = getSuccessors(url);
		res.addAll(getPredecessors(url));
		return res;
	}
    public static ArrayList<Blog> getAllBlogs() throws SQLException {
        if (stmtGetAllBlogs == null) {
            stmtGetAllBlogs =
                conn.prepareStatement("SELECT (id, url) FROM blogs;");
        }
        ResourceUser<PreparedStatement, ArrayList<Blog>, SQLException> user =
            new ResourceUser<PreparedStatement, ArrayList<Blog>, SQLException>() {

                public ArrayList<Blog> run(PreparedStatement stmt) throws SQLException {
                    ArrayList<Blog> res = new ArrayList<Blog>();
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.first()) return res;
                    res.add(new Blog(rs.getInt(1), rs.getString(2)));
                    while (rs.next()) {
                        res.add(new Blog(rs.getInt(1), rs.getString(2)));
                    }
                    return res;
                }
            
        };
        return Using.using(stmtGetAllBlogs, user);
    }
    public static ArrayList<Link> getAllLinks() throws SQLException {
        if (stmtGetAllLinks == null) {
            stmtGetAllLinks =
                conn.prepareStatement("SELECT (srcid, destid, type) FROM links;");
        }
        ResourceUser<PreparedStatement, ArrayList<Link>, SQLException> user =
            new ResourceUser<PreparedStatement, ArrayList<Link>, SQLException>() {

                public ArrayList<Link> run(PreparedStatement stmt) throws SQLException {
                    ArrayList<Link> res = new ArrayList<Link>();
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.first()) return res;
                    res.add(new Link(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
                    while (rs.next()) {
                        res.add(new Link(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
                    }
                    return res;
                }            
        };
        return Using.using(stmtGetAllLinks, user);
    }
    
}
