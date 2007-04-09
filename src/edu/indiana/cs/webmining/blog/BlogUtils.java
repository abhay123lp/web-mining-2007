package edu.indiana.cs.webmining.blog;

import java.net.MalformedURLException;
import java.net.URL;

public class BlogUtils {

    /**
     * Sanitizes a URL:
     * - Removes leading 'www.'
     * - (TODO) Converts www.livejournal.com/username to username.livejournal.com
     *
     * @param url Original URL
     * @return The sanitized URL
     * @throws MalformedURLException
     */
    public static String sanitizeURL(String url) throws MalformedURLException {
        URL fullURL = new URL(url);
        String temp = (fullURL.getHost() + fullURL.getPath()).toLowerCase();
        if (temp.startsWith("www.")) {
            return temp.substring(4);
        };
        return temp;
    }

}
