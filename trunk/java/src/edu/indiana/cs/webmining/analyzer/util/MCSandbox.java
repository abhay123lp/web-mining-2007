package edu.indiana.cs.webmining.analyzer.util;

import edu.indiana.cs.webmining.analyzer.JungController;
import edu.indiana.cs.webmining.bean.Blog;
import edu.indiana.cs.webmining.bean.LinkedBlog;
import edu.indiana.cs.webmining.db.DBManager;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class MCSandbox {

    private static ArrayList<Vertex> makeVerticesFromLinkedBlogs(Collection<LinkedBlog> linkedblogs, JungController jc) {
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();

        for (LinkedBlog lblog : linkedblogs) {
            Vertex v = jc.makeVertex(lblog.getBlog());
            vertices.add(v);
        }

        return vertices;
    }

    private static void AddVerticesToGraph(Collection<Vertex> vertices, DirectedSparseGraph g) {
        for (Vertex v : vertices) {
            try {
                g.addVertex(v);
            }
            catch (Exception e) {
            }
        }
    }

    private static void AddEdgesToGraph(Collection<DirectedEdge> edges, DirectedSparseGraph g) {
        for (DirectedEdge e : edges) {
            try {
                g.addEdge(e);
            }
            catch (Exception x) {
            }


        }
    }


    public static DirectedSparseGraph getNeighborsGraph(String blog, JungController jc) {
        try {
            DBManager dbman = jc.getDBcontroller();

            DirectedSparseGraph descTree = new DirectedSparseGraph();

            Blog b = new Blog(0, blog);
            Vertex sourceVertex = jc.makeVertex(b);
            descTree.addVertex(sourceVertex);

            // Create Downstream Level 1
            Collection<LinkedBlog> lb = dbman.getSuccessors(b.getUrl());
            if (lb == null) {
                System.err.println("getSuccessors returned null");
            }
            ArrayList<Vertex> D1_Vertices = makeVerticesFromLinkedBlogs(lb, jc);
            AddVerticesToGraph(D1_Vertices, descTree);

            ArrayList<DirectedEdge> O_D1_Edges = jc.createEdges(sourceVertex, D1_Vertices);
            AddEdgesToGraph(O_D1_Edges, descTree);


            for (Vertex v : D1_Vertices) {
                String expand_url = (String) v.getUserDatum("url");
                lb = dbman.getSuccessors(expand_url);

                ArrayList<Vertex> D2_Vertices = makeVerticesFromLinkedBlogs(lb, jc);
                AddVerticesToGraph(D2_Vertices, descTree);

                ArrayList<DirectedEdge> D1_D2_Edges = jc.createEdges(v, D2_Vertices);
                AddEdgesToGraph(D1_D2_Edges, descTree);
            }
            dbman.closeConnection();
            return descTree;
        }
        catch (SQLException e) {
            System.err.println("Database failure: " + e.getMessage());
            return null;
        }

    }


    @SuppressWarnings("unchecked")
    public static HashMap<String, Double> getFOAF(DirectedSparseGraph descTree, JungController jc, String sourceUrl) {
        try {


            HashMap<String, Integer> count = new HashMap<String, Integer>();
            HashMap<String, Double> scores = new HashMap<String, Double>();

            HashMap<String, Integer> InDegrees = new HashMap<String, Integer>();


            int intcount = 0;
            int nodecount = 0;

            Vertex source = jc.getVertexByURL(sourceUrl);

            Set<Vertex> succSet = source.getSuccessors();
            Iterator<Vertex> succIter = succSet.iterator();

            ArrayList<Vertex> D1_Vert = new ArrayList();

            DBManager dbman = jc.getDBcontroller();
            int Ka = dbman.getOutDegree(sourceUrl);

            while (succIter.hasNext()) {
                D1_Vert.add(succIter.next());
            }


            for (Vertex v : D1_Vert) {
                succSet = v.getSuccessors();
                succIter = succSet.iterator();

                ArrayList<Vertex> D2_Vert = new ArrayList();
                while (succIter.hasNext()) {
                    Vertex node = succIter.next();
                    String url = jc.getLabel(node);

//                  Record degrees of Each Nodes
                    if (!(InDegrees.containsKey(url))) {
                        int deg = dbman.getInDegree(url);
                        InDegrees.put(url, deg);
                        //System.out.println(url + ": " + deg);
                    }

                    // Count Total # of Nodes
                    if (count.containsKey(url)) {
                        count.put(url, count.get(url) + 1);
                    } else {
                        count.put(url, 1);
                        nodecount++;
                    }
                }
            }


            int N = dbman.getBlogCount();
            //int N = descTree.numVertices();
            for (String url : InDegrees.keySet()) {
                int Kb = InDegrees.get(url);
                int Q = count.get(url);

                // Secret Sauce
                double score = (Math.pow(Ka, Q) * Math.pow(((N - 2.0 - Ka) / (N - 2.0)), (Kb - Q)) / Math.pow((N - 2.0), Q));

                scores.put(url, score);


            }


            return scores;

        }
        catch (SQLException e) {
            System.err.println("Database failure: " + e.getMessage());
            return null;
        }


    }


    private static HashMap<String, Integer> neighbors(String blog) {
        try {
            JungController jc = new JungController();
            DBManager dbman = jc.getDBcontroller();

            DirectedSparseGraph descTree = new DirectedSparseGraph();

            // NorthAmericanPatriot
            // BusyMom
            Blog b = new Blog(0, blog);
            Vertex sourceVertex = jc.makeVertex(b);
            descTree.addVertex(sourceVertex);

            HashMap<String, Integer> count = new HashMap<String, Integer>();
            HashMap<String, Integer> degree = new HashMap<String, Integer>();

            // Create Downstream Level 1
            Collection<LinkedBlog> lb = dbman.getSuccessors(b.getUrl());
            Collection<Vertex> D1_Vertices = makeVerticesFromLinkedBlogs(lb, jc);
            AddVerticesToGraph(D1_Vertices, descTree);

            Collection<DirectedEdge> O_D1_Edges = jc.createEdges(sourceVertex, D1_Vertices);
            AddEdgesToGraph(O_D1_Edges, descTree);

            int intcount = 0;
            int nodecount = 0;

            for (Vertex v : D1_Vertices) {
                String expand_url = (String) v.getUserDatum("url");
                lb = dbman.getSuccessors(expand_url);

                for (LinkedBlog lblog : lb) {
                    String url = lblog.getBlog().getUrl();

                    // Record Degree of Each Nodes
                    if (!(degree.containsKey(url))) {
                        int deg = dbman.getOutDegree(url);
                        degree.put(url, deg);
                        //System.out.println(url + ": " + deg);
                    }

                    // Count Total # of Nodes
                    if (count.containsKey(url)) {
                        count.put(url, count.get(url) + 1);
                    } else {
                        count.put(url, 1);
                        nodecount++;
                    }
                }
                ArrayList<Vertex> D2_Vertices = makeVerticesFromLinkedBlogs(lb, jc);
                AddVerticesToGraph(D2_Vertices, descTree);

                ArrayList<DirectedEdge> D1_D2_Edges = jc.createEdges(v, D2_Vertices);
                AddEdgesToGraph(D1_D2_Edges, descTree);
                dbman.closeConnection();
            }

            System.out.println("Count: " + nodecount + "\n");
            //edu.uci.ics.jung.algorithms.importance.HITS hits = new edu.uci.ics.jung.algorithms.importance.HITS(descTree);

            try {
                BufferedWriter bw1 = new BufferedWriter(new FileWriter("/home/gonzo/results/" + blog + ".csv"));
                System.out.println(count.size());
                for (String s : count.keySet()) {
                    bw1.write(s + "," + count.get(s) + "\n");
                }
                bw1.close();
            }

            catch (Exception e) {
                int i = 0;
            }

            //hits.evaluate();
            //hits.printRankings(true, true);

            PajekNetWriter pw = new PajekNetWriter();


            try {
                pw.save(descTree, "/home/gonzo/pajek.net", jc, jc);
            }
            catch (Exception e) {
                System.err.println("File Not Written");
            }

            System.out.println("Complete");

            return count;
        }
        catch (SQLException e) {
            System.err.println("Database failure: " + e.getMessage());
            return null;
        }
    }


    public static void main(String[] args) {

        //String blog1 = "theblacknewyorker.blogspot.com";
        //String blog2 = "www.chronopolisnewyork.com";

        //String blog1 = "muslims-r-us.blogspot.com";
        //String blog2 = "martijn.religionresearch.org";

        //String blog1 = "www.conservativelife.com";
        //String blog2 = "rightfaith.blogspot.com";

        //String blog1 = "www.photojunkie.ca";
        //String blog2 = "toronto.photobloggers.org";

        //String blog1 = "toronto.metblogs.com";
        //String blog2 = "mividaentoronto.blogspot.com";

        String blog1 = "busymom.net";
        //String blog2 = "www.talkleft.com";


        JungController jc;
        try {
            jc = new JungController();
            DBManager dbm = new DBManager();
            DirectedSparseGraph descTree = getNeighborsGraph(blog1, jc);

            HashMap<String, Double> foaf = getFOAF(descTree, jc, blog1);
            toFileScore(foaf, blog1);


            edu.uci.ics.jung.algorithms.importance.HITS hits = new edu.uci.ics.jung.algorithms.importance.HITS(descTree);
            hits.setUseAuthorityForRanking(true);
            hits.setRemoveRankScoresOnFinalize(false);
            hits.evaluate();

//            HITS companion = new edu.uci.ics.jung.algorithms.importance.HITS(JungController.createCompanionGraph(blog1).getGraph());
//            companion.setUseAuthorityForRanking(true);
//            companion.setRemoveRankScoresOnFinalize(false);
//            companion.evaluate();

            edu.uci.ics.jung.algorithms.importance.PageRank pageRank = new edu.uci.ics.jung.algorithms.importance.PageRank(descTree, .15, null);
            pageRank.setRemoveRankScoresOnFinalize(false);
            pageRank.evaluate();

            Set<Vertex> succSet = descTree.getVertices();
            Iterator<Vertex> succIter = succSet.iterator();

            ArrayList<Vertex> vertices = new ArrayList();
            while (succIter.hasNext()) {
                Vertex v = succIter.next();
                double score = hits.getRankScore(v);
                String url = jc.getLabel(v);
                int deg = dbm.getInDegree(url);
                System.out.println(url + "," + score + ", " + deg);

            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void toFileScore(HashMap<String, Double> score, String url) {

        try {
            DBManager dbm = new DBManager();

            BufferedWriter bw1 = new BufferedWriter(new FileWriter("/home/gonzo/results/" + url + ".csv"));
            System.out.println(score.size());
            for (String s : score.keySet()) {
                int deg = dbm.getInDegree(s);
                bw1.write(s + "," + score.get(s) + ", " + deg + "\n");
            }
            bw1.close();
        }

        catch (Exception e) {
            int i = 0;
        }

    }

    private static void toFilePajek(DirectedSparseGraph descTree, JungController jc) {
        PajekNetWriter pw = new PajekNetWriter();

        try {
            pw.save(descTree, "/home/gonzo/pajek.net", jc, jc);
        }
        catch (Exception e) {
            System.err.println("File Not Written");
        }

        System.out.println("Complete");
    }


}
