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

package edu.indiana.cs.webmining.blog.impl;

import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.blog.BlogProcessingSystem;
import edu.indiana.cs.webmining.blog.BlogUtils;
import edu.indiana.cs.webmining.db.ConnectionPool;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Mar 31, 2007
 */


public class BlogDBManager {

    private ConnectionPool connectionPool;
    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbURL = "jdbc:mysql://localhost/";

    private static long totalCount = 0;

    private ConcurrentHashMap<String, Integer> blogIDList = new ConcurrentHashMap<String, Integer>();

    private static BlogDBManager blogDBManager;
    private static final String SQL_INSERT_EXT_BLOG = "INSERT IGNORE INTO extblogs (url, internal_id) VALUES (?, ?);";
    private static final String SQL_INSERT_BLOG_LINK = "INSERT IGNORE INTO links (srcid, destid) VALUES (?, ?);";


    private BlogDBManager() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("etc/sql-silo-echintha.prop"));

        try {
            connectionPool = new ConnectionPool(props.getProperty("driverClassName"), props.getProperty("url"), props.getProperty("username"), props.getProperty("password"), 15, 5,
                    true);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);

        }

    }

    public static BlogDBManager getInstance() throws IOException {
        if (blogDBManager == null) {
            blogDBManager = new BlogDBManager();
        }
        return blogDBManager;
    }

    public void insertBlogLinks(String source, String[] destinationURLs) throws SQLException, MalformedURLException {
        Connection connection = connectionPool.getConnection();

        // first let's add the source blog url
        addSingleExternalBlog(source, connection);

        // then add the destination blog urls
        addMutlipleExternalBlogs(connection, destinationURLs);

        // finally let's add the links between them
        addLink(source, destinationURLs, connection);
        connectionPool.free(connection);

        totalCount += destinationURLs.length;

        System.out.println("Total processed pages " + BlogProcessingSystem.totatProcessedPageCount);
        System.out.println("Total blogs saved = " + totalCount);

    }

    private void addLink(String src, String dest[], Connection connection) throws MalformedURLException {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_BLOG_LINK);

            for (String destinationURL : dest) {
                preparedStatement.setInt(1, blogIDList.get(BlogUtils.sanitizeURL(src)));
                preparedStatement.setInt(2, blogIDList.get(BlogUtils.sanitizeURL(destinationURL)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Link addition failed:");
            System.err.println(e.getMessage());
        }
    }

    private void addMutlipleExternalBlogs(Connection connection, String[] destinationURLs) throws SQLException, MalformedURLException {
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_EXT_BLOG);
        for (String destinationURL : destinationURLs) {
            String sanitizedURL = BlogUtils.sanitizeURL(destinationURL);
            preparedStatement.setString(1, sanitizedURL);
            preparedStatement.setInt(2, getBlogID(sanitizedURL, connection));
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();

    }

    private int addSingleExternalBlog(String url, Connection connection) throws SQLException, MalformedURLException {
        PreparedStatement stmtAddExtBlog = connection.prepareStatement(SQL_INSERT_EXT_BLOG);
        int internalID = getBlogID(BlogUtils.sanitizeURL(url), connection);

        stmtAddExtBlog.setString(1, url);
        stmtAddExtBlog.setInt(2, internalID);

        stmtAddExtBlog.execute();

        stmtAddExtBlog.close();
        return internalID;
    }

    private int getBlogID(String url, Connection connection) throws SQLException {

        // if the blog id is in the cache get it and return
        if (blogIDList.contains(url)) return blogIDList.get(url);

        // else get it from the db
        PreparedStatement stmtGetBlogID = connection.prepareStatement("SELECT id FROM blogs "
                + "WHERE url=?;");
        stmtGetBlogID.setString(1, url);

        int res;
        ResultSet results = stmtGetBlogID.executeQuery();
        if (results.next()) {
            res = results.getInt(1);
        } else {
            res = addBlog(url, connection);
        }
        results.close();
        stmtGetBlogID.close();
        blogIDList.put(url, res);
        return res;
    }

    private int addBlog(String url, Connection connection) throws SQLException {
        PreparedStatement stmtAddBlog = connection.prepareStatement("INSERT INTO blogs (url) "
                + "VALUES (?)");
        stmtAddBlog.setString(1, url);

        stmtAddBlog.executeUpdate();
        stmtAddBlog.close();

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM blogs WHERE url=?;");
        preparedStatement.setString(1, url);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public static void main(String[] args) {
        try {
            BlogDBManager blogDBManager = BlogDBManager.getInstance();
            blogDBManager.insertBlogLinks("http://bloglines.com/blogs/chinthaka", new String[]{"http://dummy.org", "http://test.org"});
        } catch (IOException e) {
            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public Integer getURLType(String pageURL) throws SQLException {

        Connection connection = connectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement("select type from URL_Cache " +
                "where url=?");
        statement.setString(1, pageURL);
        ResultSet resultSet = statement.executeQuery();

        Integer returnValue = null;
        if (resultSet.next()) {
            returnValue = resultSet.getInt("type");
        }

        statement.close();
        connectionPool.free(connection);

        return returnValue;
    }

    public void addURLType(String pageURL, int urlType) throws SQLException {
        Connection connection = connectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO URL_Cache values(?, ?)");
        statement.setString(1, pageURL);
        statement.setInt(2, urlType);
        statement.executeUpdate();
        statement.close();
        connectionPool.free(connection);

    }

    /**
     * This will retrieve a url from the frontier to be fetched
     *
     * @return
     */
    public synchronized String getNextURLToBeFetched() {
        String nextUrlToBeFetched = null;
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement selectStatement = connection.prepareStatement("SELECT Url FROM Frontier where StatusCode='" + Constants.STATUS_TO_BE_FETCHED + "' limit 1");
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Frontier SET StatusCode='" + Constants.STATUS_FETCHING + "' WHERE Url='?'");

            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                nextUrlToBeFetched = resultSet.getString("Url");
                preparedStatement.setString(1, nextUrlToBeFetched);
                preparedStatement.execute();
            }

            resultSet.close();
            selectStatement.close();
            preparedStatement.close();
            connectionPool.free(connection);

            return nextUrlToBeFetched;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nextUrlToBeFetched;
    }

    public void setURLFetched(String urlToBeFetched, String fileName) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Frontier SET " +
                    "StatusCode='" + Constants.STATUS_FETCHED + "', " +
                    "FileName='" + fileName + "' " +
                    "WHERE Url='" + urlToBeFetched + "'");
            preparedStatement.execute();

            preparedStatement.close();
            connectionPool.free(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void setFetchingCancelled(String urlToBeFetched1) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Frontier SET " +
                    "StatusCode='" + Constants.STATUS_FAILED + "', " +
                    "WHERE Url='" + urlToBeFetched1 + "'");
            preparedStatement.execute();
            preparedStatement.close();
            connectionPool.free(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSeedUrls(String[] seedUrls) {
        try {
            Connection connection = connectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Frontier (Url, StatusCode)" +
                    " VALUES (?, ?)");

            for (String seedURL : seedUrls) {
                preparedStatement.setString(1, seedURL);
                preparedStatement.setString(2, Constants.STATUS_TO_BE_FETCHED);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            connectionPool.free(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
