package edu.indiana.cs.webmining.blog;

import java.net.MalformedURLException;
import java.net.URL;

public class BlogUtils {

    /**
     * Sanitizes a URL:
     * - Removes leading 'www.'
     * - Removes trailing '/'
     * - (TODO) Converts www.livejournal.com/username to username.livejournal.com
     *
     * @param url Original URL
     * @return The sanitized URL
     * @throws MalformedURLException 
     * @throws MalformedURLException
     */
    public static String sanitizeURL(String url) throws MalformedURLException  {
        URL fullURL;
        try {
            fullURL = new URL(url);
        } catch (MalformedURLException e) {
            // Most common error is the URL lacking the protocol specification
            fullURL = new URL("http://" + url);
        }

        String temp = (fullURL.getHost() + fullURL.getPath()).toLowerCase();
        if (temp.startsWith("www.")) {
            temp = temp.substring(4);
        };
        if (temp.endsWith("/")) {
            temp = temp.substring(0, temp.length()-1);
        }
        return temp;
    }

    /**
     * Runs the URL sanitizer from the console
     * 
     * @param urls The URLs to sanitize
     */
    public static void main(String[] urls) {
        if (urls.length == 0) {
            System.out.println("Usage: java BlogUtils [urls]");
        }
        for (String url: urls) {
            
            try {
                System.out.println(url + "\t=>\t" + sanitizeURL(url));
            } catch (MalformedURLException e) {
                System.err.println(url + " is malformed");
            }
        }
    }
}
