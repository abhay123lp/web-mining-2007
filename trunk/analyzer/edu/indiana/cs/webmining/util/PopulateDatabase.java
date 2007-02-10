package edu.indiana.cs.webmining.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

public class PopulateDatabase {

	private static Connection conn;

	private static boolean addNode(String url) throws SQLException {
		String queryStr = "INSERT INTO blogs (url) "
			+ "VALUES ('" + url + "');";
		
		Statement stmt = conn.createStatement();
		return stmt.execute(queryStr);
	}
	
	private static int getNodeID(String url) throws SQLException {
		
		System.out.println("Finding ID for " + url);
		
		String queryStr = "SELECT id FROM blogs "
			+ "WHERE url='" + url + "';";
		int res;
		
		Statement stmt = conn.createStatement();
		ResultSet results = stmt.executeQuery(queryStr);
		if (results.first()) {
			res = results.getInt(1);
		} else {
			if (addNode(url)) {
				res = getNodeID(url);
			} else {
				throw new SQLException("Newly-inserted node not found");
			}
		}
		return res;
	}
	private static void addLink(String src, String dest) {		
		try {
			int srcid = getNodeID(src);
			int destid = getNodeID(dest);
			String queryStr = "INSERT INTO links (srcid, destid) "
				+ "VALUES (" + srcid + ", " + destid + ");";
			
			Statement stmt = conn.createStatement();
			stmt.execute(queryStr);
		} catch (SQLException e) {
			System.err.println("Insertion failed:");
			System.err.println(e.getMessage());
		}
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
					//tokens = new StringTokenizer()
					//tokens = new StringTokenizer(line);
					//dest = tokens.nextToken();
					//src = tokens.nextToken();
					tokens = line.split("[\\s,]+");
					src = tokens[1];
					dest = tokens[0];
					System.out.println("|" + src + "|" + dest + "|");
					//addLink(src, dest);
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
		

	}

}
