package edu.indiana.cs.webmining.crawler.filter;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

public class HeritrixTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        UURI uuri;
        try {
            uuri = UURIFactory.getInstance("http://www.foo.bar:8080/a/b/c.html");
            System.out.println(uuri);
            System.out.println("path=" + uuri.getPath());
            System.out.println("name=" + uuri.getName());
        } catch (URIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
