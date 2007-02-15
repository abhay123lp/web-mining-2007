package edu.indiana.cs.webmining.db;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import edu.indiana.cs.webmining.bean.Blog;
import edu.indiana.cs.webmining.util.ResourceUser;
import edu.indiana.cs.webmining.util.Using;

public class DBManager {

	private static Connection conn;

    /**
     * Returns a Connection
     * @return Connection
     * @throws SQLException
     */
	public static Connection getConnection() throws SQLException {
		if (conn == null) {
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
        Statement stmt = getConnection().createStatement();
        ResourceUser<Statement, ArrayList<Blog>, SQLException> user =
            new ResourceUser<Statement, ArrayList<Blog>, SQLException>() {

                public ArrayList<Blog> run(Statement stmt) throws SQLException {
                    ArrayList<Blog> res = new ArrayList<Blog>();
                    String connStr = "SELECT (id, url) FROM blogs;";
                    ResultSet rs = stmt.executeQuery(connStr);
                    if (!rs.first()) return res;
                    res.add(new Blog(rs.getInt(1), rs.getString(2)));
                    while (rs.next()) {
                        res.add(new Blog(rs.getInt(1), rs.getString(2)));
                    }
                    return res;
                }
            
        };
        return Using.using(stmt, user);
    }
}
