package spider.util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * To be used in conjunction with HTMLParser. The XML created using the HTML parser can be used as
 * input to the XML parser. DOM tree based parsing is used
 *
 * @author Gautam Pant
 * @see HTMLParser
 */
public class XMLParser {

    private String xml = null;
    private File f = null;
    private String text = null;
    private Document doc = null;
    private int maxLevel = 20; //maximum level(depth) of recursion

    /**
     * a constructor that take in a file to be parsed
     */
    public XMLParser(File f) {
        this.f = f;
    }

    /**
     * a constructor that take in a text to be parsed
     */
    public XMLParser(String text) {
        this.text = text;
    }

    /**
     * get the contents of the document that is being parsed
     */
    public String getContents() {
        if (f != null) {
            try {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                StringBuffer content = new StringBuffer("");
                int BUF_SIZE = 1024;
                char[] buffer = new char[BUF_SIZE]; //2KB buffer
                int nchars = 0;
                while ((nchars = br.read(buffer)) > 0) {
                    String text = new String(buffer, 0, nchars);
                    //text = text.trim();
                    content.append(text);
                    buffer = new char[BUF_SIZE];
                }
                br.close();
                return content.toString();
            } catch (Exception e) {
                return null;
            }
        } else if (text != null) {
            return text;
        } else {
            return null;
        }
    }

    /**
     * @return true if success, false if failure - boolean
     * @see Document
     *      opens the given xml document (file or string) and return the document object
     */
    public boolean startParser() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            //get the document
            if (f != null) {
                doc = db.parse(f);
            } else if (text != null) {
                doc = db.parse(new InputSource(new StringReader(text)));
            } else {
                return false;
            }

        } catch (Exception e) {
            //System.err.println("Could not parse file: "+f.getName());
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * get links from given XML (html). startParser() must be called before call to this method.
     *
     * @return an array of URLs - String[]
     * @see #startParser()
     */
    public String[] getLinks() {
        // if the document is null
        if (doc == null) {
            return null;
        }

        //hashtable to collect the links
        Hashtable links = new Hashtable();

        //recursively traverse the DOM tree to find the links
        traverseLinks(doc, links, 0);

        //store the links in a String array
        Enumeration keys = links.keys();
        String[] urls = new String[links.size()];
        int i = 0;
        while (keys.hasMoreElements()) {
            urls[i] = (String) keys.nextElement();
            i++;
        }

        return urls;
    }

    /**
     * get text from the given XML (html)
     *
     * @return - the text in the document - String
     */
    public String getText() {
        StringBuffer textBuffer = new StringBuffer("");
        // if the document is null
        if (doc == null) {
            return null;
        }

        //recursively traverse the DOM tree to find the text
        traverseText(doc, textBuffer, 0);
        return textBuffer.toString().trim();
    }

    /**
     * provides links with context
     * depth of aggregation node is based on rel_depth
     */
    public Hashtable getLinkContext(int rel_depth) {
        Hashtable ht = new Hashtable();
        if (doc == null) {
            return null;
        }

        //recursively traverse the DOM tree to find the text
        traverseLinkContext(doc, ht, rel_depth, 0);
        return ht;
    }

    /**
     * provides a given link's context at different levels in the DOM tree
     */
    public Hashtable getLinkContext(String look) {
        Hashtable ht = new Hashtable();
        if (doc == null) {
            return null;
        }

        look = Helper.getCanonical(look);
        if (look == null) {
            return null;
        }

        //recursively traverse the DOM tree to find the text
        traverseLinkContext(doc, ht, look, 0);
        return ht;
    }

    /**
     * climbs up the tree until it finds appropriate sized (w words) context
     */
    public Hashtable getLinkContextAdaptive(int w) {
        Hashtable ht = new Hashtable();
        if (doc == null) {
            return null;
        }

        //recursively traverse the DOM tree to find the text
        traverseLinkContextAdaptive(doc, ht, w, 0);
        return ht;
    }

    /**
     * returns the starting node of the DOM tree.
     * The user may do their own traversal over the DOM tree.
     */
    public Document getDocument() {
        return doc;
    }

    private void traverseLinks(Node node, Hashtable links, int level) {
        if (node == null || ((level >= maxLevel) && (maxLevel > 0))) {
            return;
        }
        //if there are no childern leave the current call
        if (!node.hasChildNodes()) {
            return;
        }
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeName().equalsIgnoreCase("a")) {
                NamedNodeMap nm = n.getAttributes();
                if (nm.getLength() > 0) {
                    Node link = nm.getNamedItem("href");
                    if (link != null) {
                        String url = link.getNodeValue();
                        links.put(url, "");
                    }
                }
            }
            traverseLinks(n, links, level + 1);
        }
    }

    private void traverseText(Node node, StringBuffer textBuffer, int level) {
        if (node == null || ((level >= maxLevel) && (maxLevel > 0))) {
            return;
        }
        //if there are no childern leave the current call
        if (!node.hasChildNodes()) {
            return;
        }
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeName().equalsIgnoreCase("text")) {
                Node t = n.getLastChild();
                if (t != null) {
                    String nodeText = t.getNodeValue().trim();
                    if (nodeText.length() > 0) {
                        textBuffer.append(nodeText + " ");
                    }
                }
            }
            traverseText(n, textBuffer, level + 1);
        }
    }


    /**
     * rel_depth is the depth of the aggegation node relative to the link
     * level is the current depth in the DOM tree
     */
    private void traverseLinkContext(Node node, Hashtable ht, int rel_depth, int level) {
        if (node == null || ((level >= maxLevel) && (maxLevel > 0))) {
            return;
        }
        //if there are no childern leave the current call
        if (!node.hasChildNodes()) {
            return;
        }

        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            //System.out.println(n.getNodeName()+":"+level);
            if (n.getNodeName().equalsIgnoreCase("a")) {
                NamedNodeMap nm = n.getAttributes();
                if (nm.getLength() > 0) {
                    Node link = nm.getNamedItem("href");
                    if (link != null) {
                        //get the url
                        String url = link.getNodeValue();

                        //get the context
                        StringBuffer context = new StringBuffer("");

                        //based on the given aggregation node depth find appropriate context of the current link
                        Node agg_node = n;
                        for (int j = 0; j < rel_depth; j++) {
                            agg_node = agg_node.getParentNode();
                        }

                        //System.out.println(agg_node.getNodeName());
                        StringBuffer linkTextBuffer = new StringBuffer("");
                        traverseText(agg_node, linkTextBuffer, level - rel_depth);
                        String linkText = linkTextBuffer.toString().trim();
                        if (ht.get(url) == null) {
                            ht.put(url, linkText.toString().trim());
                        }
                    }
                }
            }
            traverseLinkContext(n, ht, rel_depth, level + 1);
        }
    }

    /**
     * look is the URL to look for
     * provides link context from the entire aggregation path
     * level is the current depth in the DOM tree
     */
    private void traverseLinkContext(Node node, Hashtable ht, String look, int level) {
        if (node == null || ((level >= maxLevel) && (maxLevel > 0))) {
            return;
        }
        //if there are no childern leave the current call
        if (!node.hasChildNodes()) {
            return;
        }

        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            //System.out.println(n.getNodeName()+":"+level);
            if (n.getNodeName().equalsIgnoreCase("a")) {
                NamedNodeMap nm = n.getAttributes();
                if (nm.getLength() > 0) {
                    Node link = nm.getNamedItem("href");
                    if (link != null) {
                        //get the url
                        String url = link.getNodeValue();
                        if (look.equals(url)) {
                            Vector contexts = new Vector();
                            //based on the given aggregation node depth find appropriate context of the current link
                            Node agg_node = n;
                            int rel_depth = 0;
                            while (true) {
                                //System.out.println(agg_node.getNodeName());
                                StringBuffer linkTextBuffer = new StringBuffer("");
                                traverseText(agg_node, linkTextBuffer, level - rel_depth);
                                String linkText = linkTextBuffer.toString().trim();
                                String[] linkTextWords = linkText.split("[\\W+_]");
                                contexts.add(agg_node.getNodeName() + " " + linkTextWords.length + " " + linkText.toString().trim());
                                if (agg_node.getNodeName().equals("html")) {
                                    break;
                                }
                                agg_node = agg_node.getParentNode();
                                rel_depth++;
                            }
                            if (ht.get(url) == null) {
                                ht.put(url, contexts);
                                break;
                            }
                        }
                    }
                }
            }
            traverseLinkContext(n, ht, look, level + 1);
        }
    }

    /**
     * adaptively change the position of aggregation node so that it has atleast n words
     */
    private void traverseLinkContextAdaptive(Node node, Hashtable ht, int w, int level) {
        if (node == null || ((level >= maxLevel) && (maxLevel > 0))) {
            return;
        }
        //if there are no childern leave the current call
        if (!node.hasChildNodes()) {
            return;
        }

        NodeList nodes = node.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            //System.out.println(n.getNodeName()+":"+level);
            if (n.getNodeName().equalsIgnoreCase("a")) {
                NamedNodeMap nm = n.getAttributes();

                if (nm.getLength() > 0) {
                    Node link = nm.getNamedItem("href");
                    if (link != null) {
                        //get the url
                        String url = link.getNodeValue();

                        //get the context
                        StringBuffer context = new StringBuffer("");

                        //based on the given aggregation node depth find appropriate context of the current link
                        int rel_depth = 0;
                        Node agg_node = n;
                        String linkText = null;
                        StringBuffer linkTextBuffer = new StringBuffer("");
                        traverseText(agg_node, linkTextBuffer, level - rel_depth);
                        linkText = linkTextBuffer.toString().trim();
                        String[] parts = linkText.split("[\\W+_]");
                        //System.out.println(w);
                        int maxUp = 1;
                        while ((parts.length < w) && (rel_depth < maxUp)) {
                            //if we are already at the top
                            if (agg_node.getNodeName().equals("html")) {
                                break;
                            }
                            //System.out.println(agg_node.getNodeName());
                            agg_node = agg_node.getParentNode();
                            rel_depth++;
                            linkTextBuffer = new StringBuffer("");
                            traverseText(agg_node, linkTextBuffer, level - rel_depth);
                            linkText = linkTextBuffer.toString().trim();
                            parts = linkText.split("[\\W+_]");
                        }
                        if (ht.get(url) == null) {
                            ht.put(url, linkText.toString().trim());
                        }
                    }
                }
            }
			traverseLinkContextAdaptive(n, ht, w, level + 1);	
		}		
	}
	
}
