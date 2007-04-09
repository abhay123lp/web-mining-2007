package spider.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches HTTP pages and various details from its response header
 *
 * @author Gautam Pant
 */
public class Fetcher {

    /**
     * timeout in seconds
     */
    public int TIMEOUT = 10;

    /**
     * Max data to get (KB)
     */
    public int MAX_SIZE = 10;

    /**
     * the size of buffer
     */
    private int BUF_SIZE = 1024; //1KB

    /**
     * maximum redirections possible
     */
    public int max_redirect = 1;

    /**
     * fetches a page and returns a Page type object
     *
     * @param url   - the string url that needs to be fetched
     * @param eMail - e-mail address to go with the HTTP request
     * @return page - an object of the type Page
     * @see spider.util.Page
     */
    public Page fetch(String pageURL, String eMail) {
        Page pg = new Page();
        try {

            //for XML (XHTML) compliance - is there a better way to do this?
            pageURL = Pattern.compile("&amp;").matcher(pageURL).replaceAll("&");
            pageURL = Pattern.compile("&apos;").matcher(pageURL).replaceAll("'");
            pageURL = Pattern.compile("&quot;").matcher(pageURL).replaceAll("\"");


            URL url = new URL(pageURL);
            String host = url.getHost();
            //check for port - if none use 80
            int port = url.getPort();
            if (port == -1)
                port = 80;

            //check for file - if none use /
            String file = url.getFile();
            if (file == null) {
                file = "/";
            } else if (file.length() == 0) {
                file = "/";
            }
            Socket socket = new Socket();
            InetSocketAddress endPoint = new InetSocketAddress(host, port);
            socket.connect(endPoint, (TIMEOUT / 3) * 1000);
            //System.out.println("Got Socket");
            socket.setSoTimeout(TIMEOUT * 1000);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

            //a standard message to get http resource
            String getMess =
                    "GET "
                            + file
                            + " HTTP/1.0\nAccept: text/html\nConnection: close\nHost: "
                            + host
                            + "\nUser-Agent: Crawler0.1\nFrom: " + eMail + "\n";
            //System.out.println(getMess);
            out.println(getMess);
            out.flush();
            //read the HTTP response
            String line = in.readLine();
            line = line.trim();
            //System.out.println(line);

            //find the HTTP response code
            Pattern p = Pattern.compile("\\s+");
            String[] parts = p.split(line);
            int code = Integer.parseInt(parts[1]);
            //based on the HTTP response code handle the response
            switch (code) {
                case 200:
                    pg = getPage(in);
                    pg.code = 0;
                    break;
                case 301:
                    //find the redirection location if redirections is still allowed
                    if (max_redirect > 0) {
                        String new_url = getLocation(in);
                        max_redirect--;
                        //make sure that you have an absolute URL
                        //System.out.println((new URL(new URL(pageURL),new_url)).toString());
                        String newAbsURL = Helper.getCanonical((new URL(new URL(pageURL), new_url)).toString());
                        if (newAbsURL == null) {
                            break;
                        }
                        //add the new url to redirection list
                        Redirections.addElement(pageURL, newAbsURL);
                        pg = fetch(newAbsURL, eMail);
                        pg.code = 2;
                    }
                    break;
                case 302:
                    //find the redirection location if redirections is still allowed
                    if (max_redirect > 0) {
                        String new_url = getLocation(in);
                        max_redirect--;
                        //make sure that you have an absolute URL
                        String newAbsURL = Helper.getCanonical((new URL(new URL(pageURL), new_url)).toString());
                        if (newAbsURL == null) {
                            break;
                        }
                        //add the new url to redirection list
                        Redirections.addElement(pageURL, newAbsURL);
                        pg = fetch(newAbsURL, eMail);
                        pg.code = 2;
                    }
                    break;
                default:
                    pg.code = 1;
                    break;

            }
            out.close();
            in.close();
            socket.close();
            return pg;
        } catch (SocketTimeoutException se) {
            //System.out.println(pageURL+" timed out");
            //se.printStackTrace();
            pg.code = 3;
            return pg;
        }
        catch (Exception e) {
            //System.out.println(pageURL+" failed to fetch");
            //e.printStackTrace();
            return pg;
        }

    }

    //uses the given input reader to read HTML contents and note down some of the HTTP headers
    private Page getPage(BufferedReader in) throws Exception {
        Page pg = new Page(); //page to be returned
        StringBuffer content = new StringBuffer(""); //content buffer
        long last = 0; //last modified
        if (in != null) {
            boolean start = false;
            /*while ((line = in.readLine()) != null) {
                   //find the first empty line - it marks the start of content
                   line = line.trim();
                   if (line.length() == 0)
                       start = true;
                   //add lines once start is true but don't add empty lines and waste space
                   if ((start) && (line.length() != 0)) {
                       content.append(line + "\n");
                       if (content.length() >= (MAX_SIZE*1024))
                           break;
                   }
                   //find last modified
                   if (!start && last == 0) {
                       last = findLastModified(line);
                   }
               }*/

            //find the first empty line to mark the end of headers and then start reading
            String line = null;
            while ((line = in.readLine()) != null) {
                //find last modified
                if (last == 0) {
                    last = findLastModified(line);
                }
                //find the first empty line - it marks the start of content
                line = line.trim();
                if (line.length() == 0) {
                    break;
                }
            }
            pg.lastModified = last;
            char[] buffer = new char[BUF_SIZE]; //1KB buffer
            int nchars = 0;
            while ((nchars = in.read(buffer)) > 0) {
                String text = new String(buffer, 0, nchars);
                //text = text.trim();
                content.append(text);
                if (content.length() >= (MAX_SIZE * BUF_SIZE)) {
                    break;
                }
                buffer = new char[BUF_SIZE];
                pg.content = content.toString();
            }
            return pg;
        } else {
            return null;
        }
    }

    //looks for the last-modified field of HTTP header
    private long findLastModified(String line) throws Exception {
        //System.out.println(line);
        Pattern p = Pattern.compile("Last-modified:(.*)");
        Matcher m = p.matcher(line);
        if (m.find()) {
            String last = m.group(1);
            last = last.trim();
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
            try {
                Date date = df.parse(last);
                //System.out.println(date.getTime());
                return date.getTime();
            } catch (ParseException ex) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    //looks for the location field of HTTP header
    private String getLocation(BufferedReader in) throws Exception {
        String loc = null;
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            //if the header portion is over
            if (line.length() == 0) {
                return loc;
            }
            Pattern p = Pattern.compile("\\s+");
            String[] parts = p.split(line);
            if (parts[0].equalsIgnoreCase("Location:") && (parts.length == 2)) {
                loc = parts[1];
			}
		}		
		return loc;		
	} 

}
