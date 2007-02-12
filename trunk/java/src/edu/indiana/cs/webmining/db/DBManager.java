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

public class DBManager {

	private static Connection conn;

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
	public static String[] getSuccessors(String url) throws SQLException {
		String[] res;
		Statement stmt = getConnection().createStatement();
		String connStr = "SELECT url FROM links AS l JOIN blogs AS b "
			+ "ON l.destid = b.id "
			+ "WHERE srcid = (SELECT id FROM blogs "
			+ "WHERE url='" + url + "');";
		ResultSet rs = stmt.executeQuery(connStr);
		ArrayList<String> tres = new ArrayList<String>();
		for (String s_url: getResults(rs, 1)) {
			tres.add(s_url);
		}
		res = new String[tres.size()];
		return tres.toArray(res);
	}
	public static String[] getPredecessors(String url) throws SQLException {
		String[] res;
		Statement stmt = getConnection().createStatement();
		String connStr = "SELECT url FROM links AS l JOIN blogs AS b "
			+ "ON l.srcid = b.id "
			+ "WHERE destid = (SELECT id FROM blogs "
			+ "WHERE url='" + url + "');";
		ResultSet rs = stmt.executeQuery(connStr);
		ArrayList<String> tres = new ArrayList<String>();
		for (String s_url: getResults(rs, 1)) {
			tres.add(s_url);
		}
		res = new String[tres.size()];
		return tres.toArray(res);
	}
	public static String[] getNeighbors(String url) throws SQLException {
		String[] res1 = getSuccessors(url);
		String[] res2 = getPredecessors(url);
		String[] res = new String[res1.length + res2.length];
		ArrayList<String> tres = new ArrayList<String>();
		for (String u: res1) {
			tres.add(u);
		}
		for (String u: res2) {
			tres.add(u);
		}
		return tres.toArray(res);
	}
}
