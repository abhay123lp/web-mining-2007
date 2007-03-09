package edu.indiana.cs.webmining.analyzer.util;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SimpleDirectedSparseVertex;
import edu.uci.ics.jung.utils.UserData;

import java.util.HashMap;

public class JungTest {

    static DirectedSparseGraph graph;
    static HashMap<String, Vertex> urlVertexMap;
    static HashMap<Vertex, String> vertexURLMap;

    private static Vertex makeVertex(String url) {
        Vertex v = new SimpleDirectedSparseVertex();
        v.addUserDatum("url", url, UserData.CLONE);
        urlVertexMap.put(url, v);
        vertexURLMap.put(v, url);
        graph.addVertex(v);
        return v;
    }

    public static void main(String[] args) {
//        // TODO Auto-generated method stub
//
//        if (args.length < 1) {
//            System.err.println("Usage: java JungTest blogurl");
//            return;
//        }
//
//        String srcurl = args[0];
//
//        try {
//            ArrayList<String> Dm1urls = DBManager.getPredecessors(srcurl);
//            ArrayList<String> D1urls = DBManager.getSuccessors(srcurl);
//            urlVertexMap = new HashMap<String, Vertex>();
//            vertexURLMap = new HashMap<Vertex, String>();
//
//            graph = new DirectedSparseGraph();
//
//            // Add edges from source to D1
//            Vertex v1 = makeVertex(srcurl);
//            for (String url : D1urls) {
//                Vertex dv = makeVertex(url);
//                graph.addEdge(new DirectedSparseEdge(v1, dv));
//            }
//            // Add edges from Dm1 to source
//            for (String url : Dm1urls) {
//                Vertex sv = makeVertex(url);
//                graph.addEdge(new DirectedSparseEdge(sv, v1));
//            }
//
//            // Add edges from D1 to D2
//            for (String url : D1urls) {
//                Vertex sv = urlVertexMap.get(url);
//                for (String next : DBManager.getSuccessors(url)) {
//                    Vertex dv = makeVertex(next);
//                    graph.addEdge(new DirectedSparseEdge(sv, dv));
//                }
//            }
//            // Add edges from Dm2 to Dm1
//            for (String url : Dm1urls) {
//                Vertex dv = urlVertexMap.get(url);
//                for (String prev : DBManager.getPredecessors(url)) {
//                    Vertex sv = makeVertex(prev);
//                    graph.addEdge(new DirectedSparseEdge(sv, dv));
//                }
//            }
//
//            System.out.println("Adjacency graph has "
//                    + graph.numVertices() + " vertices and "
//                    + graph.numEdges() + " edges");
//            HITS hits = new HITS(graph);
//            hits.evaluate();
//            List<NodeRanking> authorities = (List<NodeRanking>) hits.getRankings();
//            int i = 0;
//            for (NodeRanking nr : authorities) {
//                // Only print top 20 ranks
//                if (i++ >= 20) break;
//                System.out.println("" + vertexURLMap.get(nr.vertex)
//                        + "\t" + nr.rankScore);
//            }
//            //hits.printRankings(true, true);
//        } catch (SQLException e) {
//            System.err.println("Database failure: " + e.getMessage());
//        }

    }

}
