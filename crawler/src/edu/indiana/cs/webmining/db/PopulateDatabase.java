package edu.indiana.cs.webmining.db;

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

public class PopulateDatabase {

	private static Connection conn;

	private static void addNode(String url) throws SQLException {
		String queryStr = "INSERT INTO blogs (url) "
			+ "VALUES ('" + url + "');";
		
		Statement stmt = conn.createStatement();
		stmt.execute(queryStr);
		stmt.close();
	}
	
	private static int getNodeID(String url) throws SQLException {
		
		//System.out.println("Finding ID for " + url);
		
		String queryStr = "SELECT id FROM blogs "
			+ "WHERE url='" + url + "';";
		int res;
		
		Statement stmt = conn.createStatement();
		ResultSet results = stmt.executeQuery(queryStr);
		if (results.first()) {
			res = results.getInt(1);
		} else {
			addNode(url);
			res = getNodeID(url);
		}
		stmt.close();
		results.close();
		return res;
	}
	private static void addLink(String src, String dest) {
		Statement stmt;
		try {
			int srcid = getNodeID(src);
			int destid = getNodeID(dest);
			String queryStr = "INSERT IGNORE INTO links (srcid, destid) "
				+ "VALUES (" + srcid + ", " + destid + ");";
			
			stmt = conn.createStatement();
			stmt.execute(queryStr);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Link addition failed:");
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Sanitizes a URL by removing within-site link etc.
	 * 
	 * @param url Original URL
	 * @return    The sanitized URL
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
			System.err.println("Usage: java PopulateDatabase filename");
			return;
		}
	
		try {
			reader = new BufferedReader(new FileReader(args[0]));
			
			try {
				conn = DBEngine.getConnection();
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
			} catch (SQLException e){
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
