package edu.indiana.cs.webmining.analyzer.util;

import edu.indiana.cs.webmining.db.DBManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class HitsTest2 {

	private static Connection conn;
	
	public static ArrayList<String> getNeighbors(String node) {
		ArrayList<String> res = new ArrayList<String>();
		
		return res;
	}
	
	public static void getGraph(String anchorNode, int maxDepth) {
		
	}
	
	private void initialize() {
	}
	public HitsTest2() {
		initialize();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			conn = DBManager.getConnection();
		} catch (SQLException e) {
			System.err.println("Cannot acquire connection");
		}

	}

}
