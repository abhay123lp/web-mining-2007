/**
 *
 */
package edu.indiana.cs.webmining.analyzer;

import edu.indiana.cs.webmining.bean.Blog;
import edu.indiana.cs.webmining.bean.Link;
import edu.indiana.cs.webmining.bean.LinkedBlog;
import edu.indiana.cs.webmining.db.DBManager;
import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SimpleDirectedSparseVertex;
import edu.uci.ics.jung.utils.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Michel Salim <msalim@cs.indiana.edu>
 * @since Feb 15, 2007
 */
public class JungController implements GraphController, VertexStringer, NumberEdgeValue {
    private DBManager dbman;
    private Graph graph;

    private HashMap<String, Vertex> urlVertexMap;
    private HashMap<Vertex, String> vertexURLMap;

//  private HashMap<String, Blog> urlBlogMap;
//  private HashMap<Blog, String> blogURLMap;

    private HashMap<Integer, Vertex> idVertexMap;

    private void initialize() {
        urlVertexMap = new HashMap<String, Vertex>();
        vertexURLMap = new HashMap<Vertex, String>();
//      urlBlogMap = new HashMap<String, Blog>();
//      blogURLMap = new HashMap<Blog, String>();
        idVertexMap = new HashMap<Integer, Vertex>();
    }

    public JungController() throws SQLException {
        dbman = new DBManager();
        initialize();
    }

    public JungController(ArrayList<Blog> blogs, ArrayList<Link> links) {
        this.graph = createGraph(blogs, links);
    }

    public DBManager getDBcontroller() {
        return dbman;
    }
    
    public Vertex makeVertex(Blog b) {
        int id = b.getId();
        String url = b.getUrl();

        if (urlVertexMap.containsKey(url)) {
            return urlVertexMap.get(url);
        }
        Vertex v = new SimpleDirectedSparseVertex();
        v.addUserDatum("url", url, UserData.SHARED);
        urlVertexMap.put(url, v);
        vertexURLMap.put(v, url);
        idVertexMap.put(id, v);
        //graph.addVertex(v);
        return v;
    }

    public Graph createGraph(ArrayList<Blog> blogs, ArrayList<Link> links) {
        Graph graph = new DirectedSparseGraph();
        for (Blog b : blogs) {
            Vertex v = makeVertex(b);
            graph.addVertex(v);
        }
        System.out.println("Nodes Added");
        int i = 0;
        for (Link l : links) {
            graph.addEdge(
                    new DirectedSparseEdge(
                            idVertexMap.get(l.getSourceId()),
                            idVertexMap.get(l.getDestId())));
            i++;
            if (i % 1000 == 0) {
                System.out.println("Edge " + i + "\n");
            }
        }
        System.out.println("Edges Added");
        return graph;
    }

    public Graph createSubGraph(Collection<Vertex> vertices, Collection<Edge> edges) {
        Graph target = new DirectedSparseGraph();

        for (Vertex v : vertices) {
            v.copy(target);
        }
        for (Edge e : edges) {
            e.copy(target);
        }
        return target;
    }


    public ArrayList<DirectedEdge> createEdges(Vertex v, Collection<Vertex> vs) {
        ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();

        for (Vertex vLoop : vs) {
            DirectedEdge newEdge = new DirectedSparseEdge(v, vLoop);
            edges.add(newEdge);
        }

        return edges;
    }


   
   
    // Subgraph static constructors
    
    /**
     * Creates companion graph as described by Dean and Herzinger:
     * - parents and parents' children (co-cited ciblings)
     * - children and children's parents (co-referencing siblings)
     */
    public static JungController createCompanionGraph(String url) throws SQLException {
        JungController jc = new JungController();
        DBManager dbman = jc.getDBcontroller();
        Blog start = dbman.getBlog(url);
        jc.graph = new DirectedSparseGraph();
               
        if (start == null) {
            // Throw exception instead?
            return jc;
        }
        
        Vertex startVertex = jc.makeVertex(start);
        jc.graph.addVertex(startVertex); 
        
        for (LinkedBlog pb: dbman.getPredecessors(url)) {
            Vertex pv = jc.makeVertex(pb.getBlog());
            jc.graph.addVertex(pv);
            DirectedEdge pe = new DirectedSparseEdge(pv, startVertex);
            pe.setUserDatum("linkType", pb.getLinkType(), UserData.SHARED);
            jc.graph.addEdge(pe);
            
            // Now get siblings
            for (LinkedBlog sb: dbman.getSuccessors(pb.getBlog().getUrl())) {
                Vertex sv = jc.makeVertex(sb.getBlog());
                jc.graph.addVertex(sv);
                DirectedEdge se = new DirectedSparseEdge(pv, sv);
                se.setUserDatum("linkType", sb.getLinkType(), UserData.SHARED);
                jc.graph.addEdge(se);
            }
            
        }
        for (LinkedBlog cb: dbman.getSuccessors(url)) {
            Vertex cv = jc.makeVertex(cb.getBlog());
            DirectedEdge ce = new DirectedSparseEdge(startVertex, cv);
            ce.setUserDatum("linkType", cb.getLinkType(), UserData.SHARED);
            jc.graph.addVertex(cv);
            jc.graph.addEdge(ce);
            
            // Now get siblings
            for (LinkedBlog sb: dbman.getPredecessors(cb.getBlog().getUrl())) {
                Vertex sv = jc.makeVertex(sb.getBlog());
                DirectedEdge se = new DirectedSparseEdge(sv, cv);
                se.setUserDatum("linkType", sb.getLinkType(), UserData.SHARED);
                jc.graph.addVertex(sv);
                jc.graph.addEdge(se);
            }
            
        }
        // Done with batch operations, return connection to pool
        dbman.closeConnection();
        return jc;
    }

    
    public Vertex getVertexByURL(String url)
    {
        return urlVertexMap.get(url);    
    }
    public String getLabel(ArchetypeVertex v) {
        // TODO Auto-generated method stub
        return vertexURLMap.get(v);
    }

    public Number getNumber(ArchetypeEdge arg0) {
        // TODO Auto-generated method stub
        return 1;
    }

    public void setNumber(ArchetypeEdge arg0, Number arg1) {
        // TODO Auto-generated method stub
        
    }

    public Graph getGraph() {
        return graph;
    }
}
