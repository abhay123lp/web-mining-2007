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

import edu.indiana.cs.webmining.bean.Blog;
import edu.indiana.cs.webmining.bean.Link;
import edu.indiana.cs.webmining.bean.LinkedBlog;
import edu.indiana.cs.webmining.blog.BlogUtils;
import edu.indiana.cs.webmining.util.ResourceUser;
import edu.indiana.cs.webmining.util.Using;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Michel Salim <msalim@cs.indiana.edu>
 * @since Feb 10, 2007
 */
public class DBManager {

    private static Connection conn;
    private static PreparedStatement stmtGetAllBlogs;
    private static PreparedStatement stmtGetAllLinks;
    private static PreparedStatement stmtGetPredecessors;
    private static PreparedStatement stmtGetSuccessors;
    private static PreparedStatement stmtAddBlog;
    private static PreparedStatement stmtAddExtBlog;
    private static PreparedStatement stmtGetBlogID;
    private static PreparedStatement stmtAddLink;
    private static ResourceUser<PreparedStatement, Integer, SQLException> blogIDuser;
    private static ResourceUser<PreparedStatement, Object, SQLException> voidUser;
    private static ResourceUser<PreparedStatement, ArrayList<Blog>, SQLException> pstmtUser;
    private static ResourceUser<PreparedStatement, ArrayList<LinkedBlog>, SQLException> pstmtLinkUser;

    
    /* Resource user getters */
    private static ResourceUser<PreparedStatement, Object, SQLException>
    getVoidUser() {
        if (voidUser == null) {
            voidUser =
                new ResourceUser<PreparedStatement, Object, SQLException>() {

                public Object run(PreparedStatement stmt) throws SQLException {
                    stmt.execute();
                    return null;
                }

            };
        }
        return voidUser;
    }

    private static void initialize() throws SQLException {
        try {
            // Load MySQL connector
            Class.forName("com.mysql.jdbc.Driver").newInstance();

//            System.out.println("JDBC driver loaded");
            
            
            FileInputStream propstream = new FileInputStream("etc/login.prop");
            
//            System.out.println("Properties file loaded");
            
            Properties prop = new Properties();
            prop.load(propstream);
            propstream.close();

            String connStr = "jdbc:mysql://localhost/blogmining?user="
                    + prop.getProperty("user")
                    + "&password="
                    + prop.getProperty("password");
            System.out.println("connStr=" + connStr);
            conn = DriverManager.getConnection(connStr);
            pstmtUser =
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
            pstmtLinkUser =
                    new ResourceUser<PreparedStatement, ArrayList<LinkedBlog>, SQLException>() {

                        public ArrayList<LinkedBlog> run(PreparedStatement stmt) throws SQLException {
                            ArrayList<LinkedBlog> res = new ArrayList<LinkedBlog>();
                            ResultSet rs = stmt.executeQuery();
                            if (!rs.first()) return res;
                            res.add(new LinkedBlog(new Blog(rs.getInt(1), rs.getString(2)), rs.getInt(3)));
                            while (rs.next()) {
                                res.add(new LinkedBlog(new Blog(rs.getInt(1), rs.getString(2)), rs.getInt(3)));
                            }
                            return res;
                        }
                    };
        } catch (Exception e) {
            throw new SQLException("Connection unavailable");
        }
    }

    /**
     * Returns a Connection
     *
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if (conn == null) {
            initialize();
        }
        return conn;
    }


    public static ArrayList<LinkedBlog> getSuccessors(final String url) throws SQLException {

        if (stmtGetSuccessors == null) {
            stmtGetSuccessors =
                    conn.prepareStatement("SELECT id, url, type FROM links AS l JOIN blogs AS b "
                            + "ON l.destid = b.id "
                            + "WHERE srcid = (SELECT id FROM blogs "
                            + "WHERE url=?);");
        }
        return Using.using(stmtGetSuccessors, pstmtLinkUser, false);
    }

    public static ArrayList<LinkedBlog> getPredecessors(final String url) throws SQLException {
        if (stmtGetPredecessors == null) {
            stmtGetPredecessors =
                    conn.prepareStatement("SELECT id, url, type FROM links AS l JOIN blogs AS b "
                            + "ON l.srcid = b.id "
                            + "WHERE destid = (SELECT id FROM blogs "
                            + "WHERE url=?);");
        }
        stmtGetPredecessors.setString(1, url);
        return Using.using(stmtGetPredecessors, pstmtLinkUser, false);
    }

    public static ArrayList<LinkedBlog> getNeighbors(String url) throws SQLException {
        ArrayList<LinkedBlog> res = getSuccessors(url);
        res.addAll(getPredecessors(url));
        return res;
    }

    public static ArrayList<Blog> getAllBlogs() throws SQLException {
        if (stmtGetAllBlogs == null) {
            stmtGetAllBlogs =
                    conn.prepareStatement("SELECT id, url FROM blogs;");
        }
        return Using.using(stmtGetAllBlogs, pstmtUser, false);
    }

    public static ArrayList<Link> getAllLinks() throws SQLException {
        if (stmtGetAllLinks == null) {
            stmtGetAllLinks =
                    conn.prepareStatement("SELECT srcid, destid FROM links;");
        }
        ResourceUser<PreparedStatement, ArrayList<Link>, SQLException> user =
                new ResourceUser<PreparedStatement, ArrayList<Link>, SQLException>() {

                    public ArrayList<Link> run(PreparedStatement stmt) throws SQLException {
                        ArrayList<Link> res = new ArrayList<Link>();
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.first()) return res;
                        res.add(new Link(rs.getInt(1), rs.getInt(2)));
                        while (rs.next()) {
                            res.add(new Link(rs.getInt(1), rs.getInt(2)));
                        }
                        return res;
                    }
                };
        return Using.using(stmtGetAllLinks, user, false);
    }

    private static void addBlog(String url) throws SQLException {
        if (stmtAddBlog == null) {
            stmtAddBlog = 
                conn.prepareStatement("INSERT INTO blogs (url) "
                        + "VALUES (?)");
        }
        stmtAddBlog.setString(1, url);
        stmtAddBlog.execute();
        return;
    }

    private static int getBlogID(String url) throws SQLException {
        
//        System.out.println("Looking up internal ID for " + url);
        if (stmtGetBlogID == null) {
            stmtGetBlogID =
                conn.prepareStatement("SELECT id FROM blogs "
                        + "WHERE url=?;");
        }
        stmtGetBlogID.setString(1, url);

        int res;
        ResultSet results = stmtGetBlogID.executeQuery();
        if (results.first()) {
            res = results.getInt(1);
        } else {
            addBlog(url);
            res = getBlogID(url);
        }
        return res;
    }
            
    public static int addExtBlog(String url) throws SQLException, MalformedURLException {
        if (stmtAddExtBlog == null) {
            stmtAddExtBlog =
                conn.prepareStatement("INSERT IGNORE INTO extblogs (url, internal_id) "
                        + "VALUES (?, ?);");
        }
        String sanitized_url = BlogUtils.sanitizeURL(url);
        int internal_id = getBlogID(sanitized_url);
        
//        System.out.println("Adding (" + url + ", "
//                + sanitized_url + ", "
//                + internal_id + ")");
        
        stmtAddExtBlog.setString(1, url);
        stmtAddExtBlog.setInt(2, internal_id);
        
        stmtAddExtBlog.execute();
        return internal_id;
    }
 
    public static void addLink(String src, String dest) throws MalformedURLException {
        try {
            final int srcid = addExtBlog(src);
            final int destid = addExtBlog(dest);
            if (stmtAddLink == null) {
                stmtAddLink =
                    conn.prepareStatement("INSERT IGNORE INTO links (srcid, destid) "
                    + "VALUES (?, ?);");
            }

            stmtAddLink.setInt(1, srcid);
            stmtAddLink.setInt(2, destid);

            Using.using(stmtAddLink, getVoidUser(), false);
        } catch (SQLException e) {
            System.err.println("Link addition failed:");
            System.err.println(e.getMessage());
        }
    }



}
