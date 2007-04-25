package edu.indiana.cs.webmining.analyzer.util;

import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.analyzer.JungController;
import edu.indiana.cs.webmining.bean.Blog;
import edu.indiana.cs.webmining.bean.LinkedBlog;
import edu.indiana.cs.webmining.db.DBManager;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetWriter;
import edu.uci.ics.jung.algorithms.importance.HITS;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MCSandbox {

	private static ExecutorService threadPool;

    // All clients share the same thread pool
	public static ExecutorService getThreadPool() {
		if (threadPool == null) {
			threadPool = Executors.newFixedThreadPool(Constants.MAX_THREADS);
		}
		return threadPool;
	}
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


    public static HashMap<String, Double> getFOAF(DirectedSparseGraph descTree, JungController jc, String sourceUrl, int method) {
        try {

            final HashMap<String, Integer> count = new HashMap<String, Integer>();
            HashMap<String, Double> scores = new HashMap<String, Double>();

            Map<String, Integer> InDegrees = Collections.synchronizedMap(new HashMap<String, Integer>());

            // Need to box this
            
            class Counter {
            	int count = 0;
            	public Counter(int start) {
            		count = start;
            	}
            	public int get() {
            		synchronized (this) {
            			return count;
            		}
            	}
            	public void inc() {
            		synchronized (this) {
            			count++;
            		}
            	}
            }
            final Counter nodecount = new Counter(0);

            Vertex source = jc.getVertexByURL(sourceUrl);

            ArrayList<Vertex> D1_Vert = new ArrayList<Vertex>();

            Set succSet = source.getSuccessors();
            for (Object o: source.getSuccessors()) {
                if (o instanceof Vertex) {
                    D1_Vert.add((Vertex)o);
                }
                
            }

            final DBManager dbman = jc.getDBcontroller();
            int Ka = dbman.getOutDegree(sourceUrl);


            final class ProcessD1 implements Runnable {

            	private JungController jc;
            	private Vertex v;
            	private Map<String, Integer> indegrees;
            	

            	public ProcessD1(JungController jc, Vertex v, Map indegrees) {
            		this.jc = jc;
            		this.v = v;
            		this.indegrees = indegrees;
            	}
            	
				public void run() {
					for (Object o: v.getSuccessors()) {
                        if (!(o instanceof Vertex)) {
                            // Should never happen, but keep Eclipse happy
                            continue;
                        }
						Vertex node = (Vertex) o;
						String url = jc.getLabel(node);
						
						// Record degrees of the node
						if (!(indegrees.containsKey(url))) {
	                        int deg;
							try {
								deg = dbman.getInDegree(url);
		                        indegrees.put(url, deg);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
	                    // Count Total # of Neighbors
	                    if (count.containsKey(url)) {
	                        count.put(url, count.get(url) + 1);
	                    } else {
	                        count.put(url, 1);
	                        nodecount.inc();
	                    }
					}
				}
            	
            }
            
            ArrayList<Future<?>> unfinished = new ArrayList<Future<?>>();
            
            for (Vertex v : D1_Vert) {
            	ProcessD1 processor = new ProcessD1(jc, v, InDegrees);
            	unfinished.add(getThreadPool().submit(processor));
            }

            for (Future<?> f : unfinished) {
            	try {
					f.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
            }
            
            int N = dbman.getBlogCount();
            //int N = descTree.numVertices();
            for (String url : InDegrees.keySet()) {
                Vertex v = jc.getVertexByURL(url);
                
                double Kb = InDegrees.get(url);                
                double Q = count.get(url);                
                double score = 0;
                int subGraphInDegree = v.inDegree();
                
                // Ka = OutDegree of Source URL
                // Q = Number of Neighbors in Common with Source
                // Kb = Global InDegree
                // Secret Sauce
                if (method == 1) {
                    //score = 1 / (Math.pow(Ka, Q) * Math.pow(((N - 2.0 - Ka) / (N - 2.0)), (Kb - Q)) * Math.pow(10, 200)) / (Math.pow((N - 2.0), Q)) * Math.pow(10, 200);
                    //double prob = 1 / Math.pow((Ka / (N-2.0)),Q) * Math.pow( ( (N-2-Ka) / (N-2)), (Kb-Q));
                    System.out.println(url + "\n");
                    double PartOne = 1;
                    for(int i=0;i<Q;i++)
                    {
                        PartOne += Math.log( Ka / (N-2) );
                        System.out.println(PartOne + "\n");
                        
                    }
                    //PartOne = Math.pow(PartOne,Math.E);
                    
                    double PartTwo = 1;
                    for(int i=0;i<(Kb-Q);i++)
                    {
                        PartTwo += Math.log( (N-2-Ka) / (N-2) );
                        
                    }
                    //PartTwo = Math.pow(PartTwo,Math.E);
                    
                    double 
                    prob = -PartOne * PartTwo;
                    prob = 1/prob;
                    
                    score = Q * prob;
                    //System.out.println(url + ", " + prob + ", " + Q + ", " + score);
                } else if (method == 2) {
                    score = Q;
                } else if (method == 3) {
                    
                 
                    score = Math.pow(Q, 2) * (subGraphInDegree / Kb);

                    //System.out.println(url + ", " + score + ", " + Kb + ", " + subGraphInDegree
                   //);
                }
                if(method==4)
                {
                    
                    score = (Q / Ka) * (subGraphInDegree / Kb);   
                    //System.out.println(url + ": " + subGraphInDegree + ", " + Kb );
                }
                if(method==Constants.SIM_ALGO_BASIC)
                {                    
                    score = (Q / Ka);
                   // System.out.println(url + ", " + (subGraphInDegree / Kb) );
                }
                scores.put(url, score);


            }


            return scores;

        }
        catch (SQLException e) {
            System.err.println("Database failure: " + e.getMessage());
            return null;
        }


    }
    

    public static void main(String[] args) {

        //String blog1 = "theblacknewyorker.blogspot.com";
        //String blog1 = "chronopolisnewyork.com";

        //String blog1 = "muslims-r-us.blogspot.com";
        //String blog2 = "martijn.religionresearch.org";

        //String blog1 = "conservativelife.com";
        //String blog2 = "rightfaith.blogspot.com";

        //String blog1 = "photojunkie.ca";
        //String blog2 = "toronto.photobloggers.org";

        //String blog1 = "toronto.metblogs.com";
        //String blog2 = "mividaentoronto.blogspot.com";

        //String blog1 = "sandysknitting.com";
        
        // String blog1 = "talkleft.com";
        
        String blog1 = "dooce.com";
        String blog2 = "boingboing.net";

        ArrayList<String> blogs = new ArrayList<String>();
//        blogs.add("busymom.net");
//        blogs.add("www.photojunkie.ca");

        blogs.add(blog1);
//        blogs.add(blog2);

        JungController jc;
        try {
            jc = new JungController();
            //DBManager dbm = new DBManager();
                        
            ArrayList< HashMap<String, Double> > arrScoreHashes = new ArrayList<HashMap<String,Double>>();
            
            
            for(String blog : blogs)
            {                           
                DirectedSparseGraph descTree = getNeighborsGraph(blog, jc);
                HashMap<String, Double> singleBlogScores = getFOAF(descTree, jc, blog, 4);                
                arrScoreHashes.add(singleBlogScores);                
                toFileScore(singleBlogScores, blog, "");
                
                
                edu.uci.ics.jung.algorithms.importance.HITS hits = new edu.uci.ics.jung.algorithms.importance.HITS(descTree);
                hits.setUseAuthorityForRanking(true);
                hits.setRemoveRankScoresOnFinalize(false);
                hits.evaluate();

               
                  Set<Vertex> succSet = descTree.getVertices();
                Iterator<Vertex> succIter = succSet.iterator();

                ArrayList<Vertex> vertices = new ArrayList();
                while(succIter.hasNext())
                {
                    Vertex v = succIter.next();
                    double score = hits.getRankScore(v);
                    String url = jc.getLabel(v);
                    
                    System.out.println(url + ", " + score );

                }
            }
            
            
           
            
            
            HashSet<String> intersection = new HashSet<String>();
            
   
            for(HashMap<String, Double> map : arrScoreHashes)
            {
                if(intersection.size() == 0)
                {
                    intersection.addAll(map.keySet());
                }
                else
                {                    
                    intersection.retainAll(map.keySet());
                }                
            }
            
           HashMap<String, Double> finalScore = new HashMap<String, Double>();
                       
           for(String url : intersection)
           {
               for(HashMap<String, Double> getScore : arrScoreHashes) 
               {
                   double val = getScore.get(url);
                   
                   if(finalScore.containsKey(url))
                   {
                       double oldScore = finalScore.get(url);
                       
                       finalScore.put(url, (val * finalScore.get(url)) ); 
                       
                   }
                   else
                   {
                       finalScore.put(url,val);                       
                   }                   
               }
    
        }
           
           
        
           
           //toFileScore(finalScore, "busymom", "");
         
            //         }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void toFileScore(HashMap<String, Double> score, String url, String token) {

        try {
            DBManager dbm = new DBManager();

            BufferedWriter bw1 = new BufferedWriter(new FileWriter("/home/gonzo/results/" + url + token + ".csv"));
            System.out.println(score.size());
            System.out.println( "Start");
            for (String s : score.keySet()) {
                int deg = dbm.getInDegree(s);
                bw1.write(s + "," + score.get(s) + ", " + deg + "\n");
            }
            bw1.close();
            System.out.println( "End");
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
