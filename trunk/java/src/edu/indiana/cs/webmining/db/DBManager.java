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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.io.File;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;

/**
 * @author Michel Salim <msalim@cs.indiana.edu>
 * @since Feb 10, 2007
 */
public class DBManager {

    private static DataSource dataSource;
    private Connection conn;
    private PreparedStatement stmtGetBlog;
    private PreparedStatement stmtGetAllBlogs;
    private PreparedStatement stmtGetAllLinks;
    private PreparedStatement stmtGetPredecessors;
    private PreparedStatement stmtGetSuccessors;
    private PreparedStatement stmtGetOutDegree;
    private PreparedStatement stmtGetInDegree;
    private PreparedStatement stmtAddBlog;
    private PreparedStatement stmtAddExtBlog;
    private PreparedStatement stmtGetBlogID;
    private PreparedStatement stmtAddLink;
    private PreparedStatement stmtGetBlogCount;
    private static ResourceUser<PreparedStatement, Object, SQLException> voidUser;
    private static ResourceUser<PreparedStatement, Integer, SQLException> intUser;
    private static ResourceUser<PreparedStatement, Collection<Blog>, SQLException> blogsUser;
    private static ResourceUser<PreparedStatement, Collection<LinkedBlog>, SQLException> linkedBlogsUser;
    private static String dbPropFileName = "etc/sql-silo-echintha.prop";


    public DBManager() throws SQLException {
        initialize();
    }


    public DBManager(String dbFileName) throws SQLException {
        dbPropFileName = dbFileName;
        initialize();
    }

    private static void initialize() throws SQLException {
        try {
            // dataSource might have already been initialized
            if (dataSource == null) {

                FileInputStream propstream = new FileInputStream(dbPropFileName);

                Properties prop = new Properties();
                prop.load(propstream);
                propstream.close();

                dataSource = BasicDataSourceFactory.createDataSource(prop);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Connection unavailable");
        }
    }

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

    private static ResourceUser<PreparedStatement, Integer, SQLException>
    getIntUser() {
        if (intUser == null) {
            intUser =
                new ResourceUser<PreparedStatement, Integer, SQLException>() {

                    public Integer run(PreparedStatement stmt) {
                        int answer = 0;
                        try {
// TODO Auto-generated method stub
                            ResultSet rs = stmt.executeQuery();
                            if (!rs.first()) {
                                // something is really really wrong, but just return 0
                                answer = 0;
                            }
                            answer = rs.getInt(1);
                            rs.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return answer;
                    }
            };
        }
        return intUser;

    }
    
    private static ResourceUser<PreparedStatement, Collection<Blog>, SQLException>
    getBlogsUser() {
        if (blogsUser == null) {
            blogsUser =
                new ResourceUser<PreparedStatement, Collection<Blog>, SQLException>() {

                    public Collection<Blog> run(PreparedStatement stmt) throws SQLException {
                        ArrayList<Blog> res = new ArrayList<Blog>();
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.first()) return res;
                        res.add(new Blog(rs.getInt(1), rs.getString(2)));
                        while (rs.next()) {
                            res.add(new Blog(rs.getInt(1), rs.getString(2)));
                        }
                        rs.close();
                        return res;
                    }

                };
        }
        return blogsUser;
    }

    private static ResourceUser<PreparedStatement, Collection<LinkedBlog>, SQLException>
    getLinkedBlogsUser() {
        if (linkedBlogsUser == null) {
            linkedBlogsUser =
                new ResourceUser<PreparedStatement, Collection<LinkedBlog>, SQLException>() {

                    public Collection<LinkedBlog> run(PreparedStatement stmt) throws SQLException {
                        ArrayList<LinkedBlog> res = new ArrayList<LinkedBlog>();
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.first()) return res;
                        res.add(new LinkedBlog(new Blog(rs.getInt(1), rs.getString(2)), rs.getInt(3)));
                        while (rs.next()) {
                            res.add(new LinkedBlog(new Blog(rs.getInt(1), rs.getString(2)), rs.getInt(3)));
                        }
                        rs.close();
                        return res;
                    }
                };

        }
        return linkedBlogsUser;
    }


    /* Prepared Statements */
    
    private PreparedStatement getStmtGetBlog() throws SQLException {
        if (stmtGetBlog == null || conn.isClosed()) {
            stmtGetBlog =
                    getConnection().prepareStatement("SELECT id FROM blogs "
                            + "WHERE url=?");
        }
        return stmtGetBlog;
    }

    private PreparedStatement getStmtGetPredecessors() throws SQLException {
        if (stmtGetPredecessors == null || conn.isClosed()) {
            stmtGetPredecessors =
                    getConnection().prepareStatement("SELECT id, url, type FROM links AS l JOIN blogs AS b "
                            + "ON l.srcid = b.id "
                            + "WHERE destid = (SELECT id FROM blogs "
                            + "WHERE url=?);");
        }
        return stmtGetPredecessors;
    }

    private PreparedStatement getStmtGetSuccessors() throws SQLException {
        if (stmtGetSuccessors == null || conn.isClosed()) {
            stmtGetSuccessors =
                    getConnection().prepareStatement("SELECT id, url, type FROM links AS l JOIN blogs AS b "
                            + "ON l.destid = b.id "
                            + "WHERE srcid = (SELECT id FROM blogs "
                            + "WHERE url=?);");
        }
        return stmtGetSuccessors;
    }
    
    private PreparedStatement getStmtGetOutDegree() throws SQLException {
        if (stmtGetOutDegree == null || conn.isClosed()) {
            stmtGetOutDegree =
                getConnection().prepareStatement("SELECT COUNT(destid) FROM links "
                        + "WHERE srcid = (SELECT id FROM blogs "
                        + "WHERE url=?);");
                        
        }
        return stmtGetOutDegree;
    }
    
    private PreparedStatement getStmtGetInDegree() throws SQLException {
        if (stmtGetInDegree == null || conn.isClosed()) {
            stmtGetInDegree =
                getConnection().prepareStatement("SELECT COUNT(srcid) FROM links "
                        + "WHERE destid = (SELECT id FROM blogs "
                        + "WHERE url=?);");
                        
        }
        return stmtGetInDegree;
    }
    
    private PreparedStatement getStmtGetBlogCount() throws SQLException {
        if (stmtGetBlogCount == null || conn.isClosed()) {
            stmtGetBlogCount =
                getConnection().prepareStatement("SELECT COUNT(id) FROM blogs;");
                        
        }
        return stmtGetBlogCount;
    }
    
    /**
     * Returns a Connection
     *
     * @return Connection
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        if ((conn != null) && !conn.isClosed()) {
            return conn;
        }
        if (dataSource == null) {
            initialize();
        }
        // Keep this around to allow re-use
        // Caller should make sure conn is released to pool
        conn = dataSource.getConnection();
        return conn;
    }


    public void closeConnection() throws SQLException {
        conn.close();
    }
    
    public Blog getBlog(String url) throws SQLException {
        Blog answer = null;
        PreparedStatement stmt = getStmtGetBlog();
        stmt.setString(1, url);
        ResultSet rs = stmt.executeQuery();
        if (rs.first()) {
            answer = new Blog(rs.getInt(1), url);
        }
        return answer;
    }


    public Collection<LinkedBlog> getSuccessors(final String url) throws SQLException {
        PreparedStatement stmt = getStmtGetSuccessors();
        stmt.setString(1, url);
        return Using.using(stmt, getLinkedBlogsUser(), false);
    }


    public Collection<LinkedBlog> getPredecessors(final String url) throws SQLException {
        PreparedStatement stmt = getStmtGetPredecessors();
        stmt.setString(1, url);
        return Using.using(stmt, getLinkedBlogsUser(), false);
    }

    public Collection<LinkedBlog> getNeighbors(String url) throws SQLException {
        Collection<LinkedBlog> res = getSuccessors(url);
        res.addAll(getPredecessors(url));
        return res;
    }
    
    public int getOutDegree(String url) throws SQLException {
        PreparedStatement stmt = getStmtGetOutDegree();
        stmt.setString(1, url);
        return Using.using(stmt, getIntUser(), false);
    }
    
    public int getInDegree(String url) throws SQLException {
        PreparedStatement stmt = getStmtGetInDegree();
        stmt.setString(1, url);
        return Using.using(stmt, getIntUser(), false);
    }
    
    public int getBlogCount() throws SQLException {
        PreparedStatement stmt = getStmtGetBlogCount();  
        return Using.using(stmt, getIntUser(), false);
    }
    
    

    public Collection<Blog> getAllBlogs() throws SQLException {
        if (stmtGetAllBlogs == null || conn.isClosed()) {
            stmtGetAllBlogs =
                    getConnection().prepareStatement("SELECT id, url FROM blogs;");
        }
        return Using.using(stmtGetAllBlogs, getBlogsUser(), false);
    }

    public ArrayList<Link> getAllLinks() throws SQLException {
        if (stmtGetAllLinks == null || conn.isClosed()) {
            stmtGetAllLinks =
                    getConnection().prepareStatement("SELECT srcid, destid FROM links;");
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
                        rs.close();
                        return res;
                    }
                };
        return Using.using(stmtGetAllLinks, user, false);
    }

    private void addBlog(String url) throws SQLException {
        if (stmtAddBlog == null || conn.isClosed()) {
            stmtAddBlog =
                    getConnection().prepareStatement("INSERT INTO blogs (url) "
                            + "VALUES (?)");
        }
        stmtAddBlog.setString(1, url);
        stmtAddBlog.execute();
        return;
    }

    private int getBlogID(String url) throws SQLException {

//      System.out.println("Looking up internal ID for " + url);
        if (stmtGetBlogID == null || conn.isClosed()) {
            stmtGetBlogID =
                    getConnection().prepareStatement("SELECT id FROM blogs "
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
        results.close();
        return res;
    }

    public int addExtBlog(String url) throws SQLException, MalformedURLException {
        if (stmtAddExtBlog == null || conn.isClosed()) {
            stmtAddExtBlog =
                    getConnection().prepareStatement("INSERT IGNORE INTO extblogs (url, internal_id) "
                            + "VALUES (?, ?);");
        }
        String sanitized_url = BlogUtils.sanitizeURL(url);
        int internal_id = getBlogID(sanitized_url);

//      System.out.println("Adding (" + url + ", "
//      + sanitized_url + ", "
//      + internal_id + ")");

        stmtAddExtBlog.setString(1, url);
        stmtAddExtBlog.setInt(2, internal_id);

        stmtAddExtBlog.execute();
        return internal_id;
    }

    public void addLink(String src, String dest) throws MalformedURLException {
        try {
            final int srcid = addExtBlog(src);
            final int destid = addExtBlog(dest);
            if (stmtAddLink == null || conn.isClosed()) {
                stmtAddLink =
                        getConnection().prepareStatement("INSERT IGNORE INTO links (srcid, destid) "
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
