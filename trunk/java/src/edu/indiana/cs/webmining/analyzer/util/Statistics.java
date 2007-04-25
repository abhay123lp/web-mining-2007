package edu.indiana.cs.webmining.analyzer.util;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;

import edu.indiana.cs.webmining.analyzer.JungController;
import edu.indiana.cs.webmining.bean.Blog;
import edu.indiana.cs.webmining.bean.Link;
import edu.indiana.cs.webmining.db.DBManager;

public class Statistics {
  
    public static void main(String[] args) {
       DBManager dbman;
    try {
        dbman = new DBManager();
        Collection<Blog> blogs = dbman.getAllBlogs();
        Collection<Link> links = dbman.getAllLinks();
        
        JungController jc = new JungController(blogs, links);
        jc.printHistogram(jc.getIndegreeHistogram(), new File("stats-indegrees.csv"));
        jc.printHistogram(jc.getOutdegreeHistogram(), new File("stats-outdegrees.csv"));
        jc.printHistogram(jc.getTotalDegreeHistogram(), new File("stats-totaldegrees.csv"));
    } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
       
    }
}
