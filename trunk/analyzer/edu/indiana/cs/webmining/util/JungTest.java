package edu.indiana.cs.webmining.util;

import java.sql.SQLException;
import java.util.HashMap;

import edu.uci.ics.jung.algorithms.importance.HITS;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SimpleDirectedSparseVertex;
import edu.uci.ics.jung.utils.UserData;

public class JungTest {

	static DirectedSparseGraph graph;
	static HashMap<String, Vertex> urlVertexMap;
	/**
	 * @param args
	 */
	
	private static Vertex makeVertex(String url) {
		Vertex v = new SimpleDirectedSparseVertex();
		v.addUserDatum("url", url, UserData.CLONE);
		urlVertexMap.put(url, v);
		graph.addVertex(v);
		return v;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length < 1) {
			System.err.println("Usage: java JungTest blogurl");
			return;
		}
		
		String srcurl = args[0];
		
		try {
			String[] D1urls = DBEngine.getSuccessors(srcurl);
			urlVertexMap = new HashMap<String, Vertex>();

			graph = new DirectedSparseGraph();

			// Add edges from source to D1
			Vertex v1 = makeVertex(srcurl);
			for (String url: D1urls) {
				Vertex dv = makeVertex(url);
				graph.addEdge(new DirectedSparseEdge(v1, dv));
			}
			
			// Add edges from D1 to D2
			for (String url: D1urls) {
				Vertex sv = urlVertexMap.get(url);
				for (String next: DBEngine.getSuccessors(url)) {
					Vertex dv = makeVertex(next);
					graph.addEdge(new DirectedSparseEdge(sv, dv));
				}
			}
			System.out.println("Adjacency graph has "
					+ graph.numVertices() + " vertices and "
					+ graph.numEdges() + " edges");
			HITS hits = new HITS(graph);
			hits.evaluate();
			hits.printRankings(true, true);
		} catch (SQLException e) {
			System.err.println("Database failure: " + e.getMessage());
		}
		
	}

}
