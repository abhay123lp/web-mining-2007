package spider.util;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.StringReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Description:
 * The class provides methods to parse an html page and convert it into an XML format
 *
 * @author Gautam Pant
 */

public class HTMLParser extends ParserCallback {

    private Vector tags = new Vector();
    private Stopper stop = null;
    private boolean stemmer = false;
    private int maxDepth = 20; //maximum depth of HTML to parse and store
    private int currentDepth = 0; //current depth in parsing

    /**
     * Constructer (if a stopper is provided - stopper alows for removing stop words)
     */
    public HTMLParser(Stopper stop) {
        this.stop = stop;
    }

    public HTMLParser() {
    }

    // implementation of callback functions - functions are called
    // when the document is parsed


    /**
     * Note the start of a tag and put the new state in the state stack
     */
    public void handleStartTag(
            HTML.Tag tag,
            MutableAttributeSet attribs,
            int pos) {
        if (currentDepth >= maxDepth) {
            //System.out.println("Max Depth Exceeded: "+currentDepth);
            return;
        }
        tags.add(new StackElement(tag, attribs));
        currentDepth++;
    }

    /**
     * Handle the end tag. Push it into the tags stack.
     * The implied calls to both handleStartTag and handleEndTag
     * help in correcting bad or missing HTML tags
     */
    public void handleEndTag(HTML.Tag tag, int pos) {
        currentDepth--;
        if (currentDepth >= maxDepth) {
            return;
        }
        tags.add(new StackElement(tag));
    }

    /**
     * Handle text. Push it into the tags stack
     */
    public void handleText(char[] text, int pos) {
        tags.add(new StackElement(new String(text)));
    }

    /**
     * non-word characters and stop words are removed from text and the modified text is returned back
     */
    String cleanText(String text) {
        String result = null;
        if (text == null)
            text = new String("");
        try {
            Pattern exp = Pattern.compile("[_\\W+]");
            String[] parts = exp.split(text);
            result = Helper.join(" ", parts).toLowerCase();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        //if a list of stop words was provided
        if (stop != null) {
            StringBuffer stopText = new StringBuffer("");
            StringTokenizer q = new StringTokenizer(result);
            while (q.hasMoreTokens()) {
                String next = q.nextToken();
                if (!stop.isStopWord(next)) {
                    stopText.append(next + " ");
                } else {
                    //System.out.println("Stop Word:"+next);
                }
            }
            result = stopText.toString();
        }
        //if stemmer is set
        Stemmer st = null;
        if (stemmer) {
            st = new Stemmer();
            result = st.stem(result);
        }
        result = Helper.escapeText(result.trim());
        return result;
    }

    /**
     * convert the html into an xml format(naive)
     * Currently all the HTML tags are kept (some corrected) but the only attribute
     * that is stored is href
     *
     * @param html string - String, the url - String
     * @return if the conversion failed, else the XML string is returned
     */
    public String htmlToXML(String html, String url) {
        currentDepth = 0;
        if (html == null)
            return null;
        StringBuffer xml = new StringBuffer();
        URL baseurl = null;
        try {
            baseurl = new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ParserDelegator pd = new ParserDelegator();
        StringReader sr = new StringReader(html);

        xml.append("<?xml version=\"1.0\"?>\n");
        //xml.append("<html_xml>\n");
        //xml.append("<url>");
        //xml.append(url);
        //xml.append("</url>\n");
        try {
            pd.parse(sr, this, true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(url);
            return null;
        }
        boolean start = false; //marks the start of html

        for (int i = 0; i < tags.size(); i++) {
            StackElement se = (StackElement) tags.get(i);
            switch (se.type) {
                case 0: //text
                    String ctext = Helper.escapeText(cleanText(se.text));
                    if (ctext != null && ctext.length() > 0) {
                        xml.append("<text>");
                        xml.append(ctext);
                        xml.append("</text>\n");
                    }
                    break;
                case 1: //start tag
                    xml.append("<" + se.tag);
                    MutableAttributeSet a = se.attribs;
                    String href = (String) a.getAttribute(HTML.Attribute.HREF);
                    URL link = null;
                    try {
                        link = new URL(baseurl, href);
                    } catch (Exception e) {
                        link = null;
                    }
                    if ((href != null) && (link != null)) {
                        String uri =
                                Helper.escapeURL(Helper.getCanonical(link.toString()));
                        // if the canonical form does not exist
                        if (uri == null) {
                            //System.out.println(link.toString()+ "rejected");
                            xml.append(">\n");
                            break;
                        } else if (uri.length() > 256) {
                            xml.append(">\n");
                            break;
                        }
                        StringBuffer attributes = new StringBuffer("");
                        attributes.append("href" + "=\"" + uri + "\" ");
                        xml.append(" " + (attributes.toString()).trim());
                    }
                    xml.append(">\n");

                    break;
                case 2: //end tag
                    xml.append("</" + se.tag + ">\n");
                    break;
            }
        }
        //xml.append("</html_xml>");
        return xml.toString();
    }

    /**
     * Returns the stemmer.
     *
     * @return boolean
     */
    public boolean isStemmer() {
        return stemmer;
    }

    /**
     * Sets the stemmer.
     *
     * @param stemmer The stemmer to set
     */
    public void setStemmer(boolean stemmer) {
		this.stemmer = stemmer;
	}

}
