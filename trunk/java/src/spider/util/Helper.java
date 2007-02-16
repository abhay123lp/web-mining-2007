package spider.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A bunch of static helper functions
 *
 * @author Gautam Pant
 */
public class Helper {

    /**
     * joins the text in a string array with a given seperator
     */
    public static String join(String seperator, String[] parts) {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.length() == 0) {
                continue;
            }
            sb.append(part);
            //if not the last part of text add the seperator in the text
            if (i != (parts.length - 1)) {
                sb.append(seperator);
            }
        }
        return sb.toString();
    }

    /**
     * joins the text in a part of string array with a given seperator
     */
    public static String join(
            String seperator,
            String[] parts,
            int start,
            int end) {
        StringBuffer sb = new StringBuffer("");
        for (int i = start; (i < parts.length) && (i <= end); i++) {
            String part = parts[i].trim();
            if (part.length() == 0) {
                continue;
            }
            sb.append(part);
            //if not the last part of text add the seperator in the text
            if (i != (parts.length - 1) && (i < end)) {
                sb.append(seperator);
            }
        }
        return sb.toString();
    }

    /**
     * get the host name from the given URL
     */
    public static String getHostName(String url) {
        //if the URL does not start with http return null
        if (!Pattern.matches("^(http://.+)", url)) {
            // not an appropriate http URI
            return null;
        }
        Pattern p = Pattern.compile("[/|:]");
        String[] parts = p.split(url);
        //find the domain
        String domain = parts[3];
        return domain;
    }

    /**
     * get the second level domain name from a given url
     *
     * @param url
     * @return the second-level domain name
     */
    public static String getDomainName(String url) {
        //if the URL does not start with http return null
        if (!Pattern.matches("^(http://.+)", url)) {
            // not an appropriate http URI
            return null;
        }
        Pattern p = Pattern.compile("[/|:]");
        String[] parts = p.split(url);
        //find the domain
        String domain = parts[3];
        parts = domain.split("\\.");
        if (parts.length > 2) {
            domain = Helper.join(".", parts, parts.length - 2, parts.length - 1);
        }
        return domain;
    }

    /**
     * get host name with port from a given URL
     */
    public static String getHostNameWithPort(String url) {
        //if the URL does not start with http return null
        if (!Pattern.matches("^(http://.+)", url)) {
            // not an appropriate http URI
            return null;
        }
        Pattern p = Pattern.compile("/");
        String[] parts = p.split(url);
        //find the domain
        String domain = parts[2];
        return domain;
    }

    /**
     * get the path from the given URL
     */
    public static String getURLPath(String url) {
        //if the URL does not start with http return null
        if (!Pattern.matches("^(http://.+)", url)) {
            // not an appropriate http URI
            return null;
        }
        Pattern p = Pattern.compile("/");
        String[] parts = p.split(url);
        //find the path
        if (parts.length > 3) {
            String path = "/" + join("/", parts, 3, parts.length - 1);
            if (url.charAt(url.length() - 1) == '/') {
                path = path + "/";
            }
            return path;
        } else {
            return "";
        }
    }

    /**
     * returns the canonical URL
     */
    public static String getCanonical(String url) {
        try {

            if (url == null) {
                return null;
            }

            //escape the special url characters first
            url = escapeURL(url);

            //if the URL does not start with http return null
            if (!Pattern.matches("^(http://.+)", url)) {
                // not an appropriate http URI
                return null;
            }

            //if you want to remove URL with question mark (this won't work with URL encoding)
            /*if (!Pattern.matches("^(http://[^?]+)", url)){
                   // not an appropriate http URI
                   return null;
               }*/

            Pattern p = Pattern.compile("/");
            String[] parts = p.split(url);

            //find the domain
            String domain = parts[2];

            //make the domain all lowercase
            domain = domain.toLowerCase();

            //find the path
            if (parts.length > 3) {
                //just store the parts that have the path in sb
                String[] sb = new String[parts.length - 3];
                for (int i = 3; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                    //throw away ".." and a directory above it
                    if (parts[i].equals("..")) {
                        sb[i - 3] = "";
                        //if there is a directory above this one in the path
                        if (sb.length > 1 && (i - 3) > 0) {
                            sb[i - 4] = "";
                        }
                    } else {
                        sb[i - 3] = parts[i];
                    }
                }

                String path = join("/", sb);

                //remove portions after # in path
                boolean fileFlag = false;
                ;
                p = Pattern.compile("(.*?)#(.*)");
                Matcher m = p.matcher(path);
                if (m.find()) {
                    //it is a file
                    fileFlag = true;
                    path = m.group(1);
                }

                //remove portions after ? in the path
                /*p = Pattern.compile("(.+?)\\?(.+)");
                    m = p.matcher(path);
                    if (m.find()){
                        path = m.group(1);
                    }*/

                //remove index.html or index.htm from path
                p =
                        Pattern.compile(
                                "(.*?)index\\..+$",
                                Pattern.CASE_INSENSITIVE);
                m = p.matcher(path);
                if (m.find()) {
                    path = m.group(1);
                }

                //if does not seem to be a file then add a / at the end
                if (!fileFlag
                        && !Pattern.matches("(.+?)\\.(.+)", path)
                        && !path.endsWith("/")
                        && path.length() > 0
                        && !path.matches(".*%3F.*")) {
                    path = path + "/";
                }

                url = "http://" + domain + "/" + path;
            } else {
                url = "http://" + domain + "/";
            }
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * escapes special characters in XML
     */
    public static String escapeText(String s) {
        if (s == null) {
            return null;
        }

        //some special characters need to be identified and replaced with codes
        s = Pattern.compile("&").matcher(s).replaceAll("&amp;");
        s = Pattern.compile("<").matcher(s).replaceAll("&lt;");
        s = Pattern.compile(">").matcher(s).replaceAll("&gt;");
        s = Pattern.compile("\"").matcher(s).replaceAll("&quot;");
        s = Pattern.compile("'").matcher(s).replaceAll("&apos;");
        return s;
    }

    /**
     * escaping special characters from URLs through URL encoding
     */
    public static String escapeURL(String s) {
        if (s == null) {
            return null;
        }

        //some special characters need to be identified and replaced with codes
        s = Pattern.compile("<").matcher(s).replaceAll("%3C");
        s = Pattern.compile(">").matcher(s).replaceAll("%3E");
        //make sure that you not escape & mutiple time
        s = Pattern.compile("&amp;").matcher(s).replaceAll("&");
        s = Pattern.compile("&quot;").matcher(s).replaceAll("\"");
        s = Pattern.compile("&apos;").matcher(s).replaceAll("'");

        s = Pattern.compile("~").matcher(s).replaceAll("%7E");
        s = Pattern.compile(" ").matcher(s).replaceAll("%20");

        s = Pattern.compile("&").matcher(s).replaceAll("&amp;");
        //need to be done for XML (I am not sure what they would mean for the URL ???)
        s = Pattern.compile("\"").matcher(s).replaceAll("&quot;");
        s = Pattern.compile("'").matcher(s).replaceAll("&apos;");
        return s;

    }

    /**
     * consine similarity between two strings (without idf)
     */
    public static double getSim(String text1, String text2) {
        String[] text1Tokens = Pattern.compile("\\s+").split(text1);
        String[] text2Tokens = Pattern.compile("\\s+").split(text2);
        Hashtable ht1 = new Hashtable();
        Hashtable ht2 = new Hashtable();
        //put the terms in hashtables with term as key and term frequency as value

        //text 1 tokens -> hash
        for (int i = 0; i < text1Tokens.length; i++) {
            if (ht1.containsKey(text1Tokens[i])) {
                ht1.put(
                        text1Tokens[i],
                        new Integer(
                                ((Integer) ht1.get(text1Tokens[i])).intValue() + 1));
            } else {
                ht1.put(text1Tokens[i], new Integer(1));
            }
        }

        //text 2 tokens -> hash
        for (int i = 0; i < text2Tokens.length; i++) {
            if (ht2.containsKey(text2Tokens[i])) {
                ht2.put(
                        text2Tokens[i],
                        new Integer(
                                ((Integer) ht2.get(text2Tokens[i])).intValue() + 1));
            } else {
                ht2.put(text2Tokens[i], new Integer(1));
            }
        }

        //find magnitude of vectors in the hashtables;
        double mag1 = 0;
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            mag1 =
                    mag1
                            + Math.pow(
                            ((Integer) ht1.get(e.nextElement())).intValue(),
                            2);
        }
        mag1 = Math.sqrt(mag1);

        double mag2 = 0;
        for (Enumeration e = ht2.keys(); e.hasMoreElements();) {
            mag2 =
                    mag2
                            + Math.pow(
                            ((Integer) ht2.get(e.nextElement())).intValue(),
                            2);
        }
        mag2 = Math.sqrt(mag2);

        //find  mathematically dot product of the two text vectors
        double mult = 0;
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            String term = (String) e.nextElement();
            if (ht2.containsKey(term)) {
                mult =
                        mult
                                + ((Integer) ht2.get(term)).doubleValue()
                                * ((Integer) ht1.get(term)).doubleValue();
            }
        }

        //find cosine of the angle between the vectors
        double cosineSim = mult / (mag1 * mag2);
        //System.out.println(mult+"  "+mag1+"  "+mag2);
        return cosineSim;
    }

    /**
     * cosine of the angle between two vectors
     */
    public static double getCosine(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            System.err.println(
                    "Warning: the vector dot product has unequal sized vectors");
            return 0;
        }

        //dot product
        double mag1 = 0;
        double mag2 = 0;
        double cos = 0;
        for (int i = 0; i < v1.length; i++) {
            cos = cos + v1[i] * v2[i];
            mag1 = mag1 + v1[i] * v1[i];
            mag2 = mag2 + v2[i] * v2[i];
            //System.out.println(v1[i]+" "+v2[i]);
        }
        if (mag1 == 0 || mag2 == 0) {
            //System.err.println("Warning: a zero magnitude vector while calculating cosine");
            return 0;
        }
        cos = cos / (Math.sqrt(mag1) * Math.sqrt(mag2));
        return cos;
    }

    /**
     * consine similarity by project text onto query space
     */
    public static double getSimInQuerySpace(String query, String text) {
        String[] text1Tokens = Pattern.compile("\\s+").split(query);
        String[] text2Tokens = Pattern.compile("\\s+").split(text);
        Hashtable ht1 = new Hashtable();
        Hashtable ht2 = new Hashtable();
        //put the terms in hashtables with term as key and term frequency as value

        //query tokens -> hash
        for (int i = 0; i < text1Tokens.length; i++) {
            if (ht1.containsKey(text1Tokens[i])) {
                ht1.put(
                        text1Tokens[i],
                        new Integer(
                                ((Integer) ht1.get(text1Tokens[i])).intValue() + 1));
            } else {
                ht1.put(text1Tokens[i], new Integer(1));
            }
        }

        //text tokens (those also in query)-> hash
        for (int i = 0; i < text2Tokens.length; i++) {
            //do not include terms that are not in query
            if (!ht1.containsKey(text2Tokens[i])) {
                continue;
            }
            if (ht2.containsKey(text2Tokens[i])) {
                ht2.put(
                        text2Tokens[i],
                        new Integer(
                                ((Integer) ht2.get(text2Tokens[i])).intValue() + 1));
            } else {
                ht2.put(text2Tokens[i], new Integer(1));
            }
        }

        //find magnitude of vectors in the hashtables;
        double mag1 = 0;
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            mag1 =
                    mag1
                            + Math.pow(
                            ((Integer) ht1.get(e.nextElement())).intValue(),
                            2);
        }
        mag1 = Math.sqrt(mag1);

        double mag2 = 0;
        for (Enumeration e = ht2.keys(); e.hasMoreElements();) {
            mag2 =
                    mag2
                            + Math.pow(
                            ((Integer) ht2.get(e.nextElement())).intValue(),
                            2);
        }
        mag2 = Math.sqrt(mag2);

        //find  mathematically dot product of the two text vectors
        double mult = 0;
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            String term = (String) e.nextElement();
            if (ht2.containsKey(term)) {
                mult =
                        mult
                                + ((Integer) ht2.get(term)).doubleValue()
                                * ((Integer) ht1.get(term)).doubleValue();
            }
        }

        //find cosine of the angle between the vectors
        double cosineSim = mult / (mag1 * mag2);
        //System.out.println(mult+"  "+mag1+"  "+mag2);
        return cosineSim;
    }

    /**
     * consine similarity between two strings - SMART atc - idf included
     */
    public static double getSim(
            String text1,
            String text2,
            Hashtable df,
            int noDocs) {
        String[] text1Tokens = Pattern.compile("\\s+").split(text1);
        String[] text2Tokens = Pattern.compile("\\s+").split(text2);
        Hashtable ht1 = new Hashtable();
        Hashtable ht2 = new Hashtable();
        //put the terms in hashtables with term as key and term frequency as value

        //text 1 tokens -> hash
        for (int i = 0; i < text1Tokens.length; i++) {
            if (ht1.containsKey(text1Tokens[i])) {
                ht1.put(
                        text1Tokens[i],
                        new Integer(
                                ((Integer) ht1.get(text1Tokens[i])).intValue() + 1));
            } else {
                ht1.put(text1Tokens[i], new Integer(1));
            }
        }

        //text 2 tokens -> hash
        for (int i = 0; i < text2Tokens.length; i++) {
            if (ht2.containsKey(text2Tokens[i])) {
                ht2.put(
                        text2Tokens[i],
                        new Integer(
                                ((Integer) ht2.get(text2Tokens[i])).intValue() + 1));
            } else {
                ht2.put(text2Tokens[i], new Integer(1));
            }
        }

        //find the tf_max for each text
        int max1 = 0;
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            int tf = ((Integer) ht1.get(e.nextElement())).intValue();
            if (tf > max1) {
                max1 = tf;
            }
        }

        //find the tf_max for each text
        int max2 = 0;
        for (Enumeration e = ht2.keys(); e.hasMoreElements();) {
            int tf = ((Integer) ht2.get(e.nextElement())).intValue();
            if (tf > max2) {
                max2 = tf;
            }
        }
        //System.out.println(max1+" "+max2);
        //if there are no words in the texts
        if (max1 == 0 || max2 == 0) {
            return 0;
        }

        //set the new tf weights and multiply idf (at components)
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            String term = (String) e.nextElement();
            int tf = ((Integer) ht1.get(term)).intValue();
            double new_tf = 0.5 + 0.5 * (((double) tf) / max1);
            if (df.get(term) != null) {
                int docFreq = ((Integer) df.get(term)).intValue();
                double idf = Math.log(noDocs / (double) docFreq);
                ht1.put(term, new Double(new_tf * idf));
            } else {
                double idf = Math.log(noDocs);
                ht1.put(term, new Double(new_tf * idf));
            }
        }

        //set the new tf weights and multiply idf (at components)
        for (Enumeration e = ht2.keys(); e.hasMoreElements();) {
            String term = (String) e.nextElement();
            int tf = ((Integer) ht2.get(term)).intValue();
            double new_tf = 0.5 + 0.5 * (((double) tf) / max2);
            if (df.get(term) != null) {
                int docFreq = ((Integer) df.get(term)).intValue();
                double idf = Math.log(noDocs / (double) docFreq);
                ht2.put(term, new Double(new_tf * idf));
            } else {
                double idf = Math.log(noDocs);
                ht2.put(term, new Double(new_tf * idf));
            }
        }

        //find magnitude of vectors in the hashtables;
        double mag1 = 0;
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            mag1 =
                    mag1
                            + Math.pow(
                            ((Double) ht1.get(e.nextElement())).doubleValue(),
                            2);
        }
        mag1 = Math.sqrt(mag1);

        double mag2 = 0;
        for (Enumeration e = ht2.keys(); e.hasMoreElements();) {
            mag2 =
                    mag2
                            + Math.pow(
                            ((Double) ht2.get(e.nextElement())).doubleValue(),
                            2);
        }
        mag2 = Math.sqrt(mag2);

        //find  mathematically dot product of the two text vectors
        double mult = 0;
        for (Enumeration e = ht1.keys(); e.hasMoreElements();) {
            String term = (String) e.nextElement();
            if (ht2.containsKey(term)) {
                mult =
                        mult
                                + ((Double) ht2.get(term)).doubleValue()
                                * ((Double) ht1.get(term)).doubleValue();
            }
        }

        //find cosine of the angle between the vectors
        double cosineSim = mult / (mag1 * mag2);
        //System.out.println(mult+"  "+mag1+"  "+mag2);
        return cosineSim;
    }

    /**
     * provides links with context
     * noWords is the number of words around a link text used for context
     */
    public static Hashtable getLinkContextWords(String content, int noWords) {
        Hashtable ht = new Hashtable();
        try {
            StringReader sr = new StringReader(content);
            BufferedReader br = new BufferedReader(sr);
            String line = null;
            Vector words = new Vector();
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                Matcher m = Pattern.compile("<text>(.*)</text>").matcher(line);
                if (m.find()) {
                    String[] terms = m.group(1).split("\\W+");
                    for (int i = 0; i < terms.length; i++) {
                        words.add(terms[i]);
                    }
                    continue;
                }
                m = Pattern.compile("(<a href=\"(.*?)\">)").matcher(line);
                if (m.find()) {
                    words.add(m.group(1));
                    continue;
                }
            }

            for (int i = 0; i < words.size(); i++) {
                String term = (String) words.get(i);
                String context = "";
                Matcher m = Pattern.compile("<a href=\"(.*?)\">").matcher(term);
                if (m.find()) {
                    //get noWords/2 above the link
                    int count = 0;
                    for (int k = i - 1; k > 0; k--) {
                        String t = (String) words.get(k);
                        Matcher match =
                                Pattern.compile("<a href=\"(.*?)\">").matcher(t);
                        if (match.find()) {
                            continue;
                        }
                        context = t + " " + context;
                        count++;
                        if (count >= noWords / 2) {
                            break;
                        }
                    }

                    //get noWords/2 after the link
                    count = 0;
                    for (int k = i + 1; k < words.size(); k++) {
                        String t = (String) words.get(k);
                        Matcher match =
                                Pattern.compile("<a href=\"(.*?)\">").matcher(t);
                        if (match.find()) {
                            continue;
                        }
                        context = context + " " + t;
                        count++;
                        if (count >= noWords / 2) {
                            break;
                        }
                    }
                    if (ht.get(m.group(1)) == null) {
                        ht.put(m.group(1), context.trim());
                    }
                }
            }

        } catch (Exception e) {
            return null;
        }
        return ht;
    }

    public static void runSystemCmd(String cmd) {
        try {
            Runtime rt = Runtime.getRuntime();
            System.out.println("Executing " + cmd);
            Process x = rt.exec(cmd);
            BufferedReader stdInput =
                    new BufferedReader(new InputStreamReader(x.getInputStream()));
            BufferedReader stdError =
                    new BufferedReader(new InputStreamReader(x.getErrorStream()));
            String line = null;
            while ((line = stdError.readLine()) != null) {
                System.out.println(line);
            }
            x.waitFor();
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
