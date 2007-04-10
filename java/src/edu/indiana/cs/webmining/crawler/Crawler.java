/* Copyright (C) 2004 The Trustees of Indiana University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */

package edu.indiana.cs.webmining.crawler;

import edu.indiana.cs.webmining.BlogCrawlingContext;
import edu.indiana.cs.webmining.Constants;
import edu.indiana.cs.webmining.blog.BlogCrawlingException;
import edu.indiana.cs.webmining.blog.impl.BlogDBManager;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import spider.util.Hashing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Apr 8, 2007
 */
public class Crawler implements Runnable {

    BlogDBManager dbManager;

    private static int crawlerCount = 0;

    private int myNumber;

    private HttpClient client;
    private BlogCrawlingContext context;
    private File dataFolder;


    public Crawler(BlogCrawlingContext context) throws BlogCrawlingException {
        dbManager = new BlogDBManager();
        this.context = context;
        this.dataFolder = context.getFileStore();
        myNumber = ++crawlerCount;
        client = new HttpClient();
    }

    public void run() {
        System.out.println("Starting Crawler " + myNumber + " ....");
        while (true) {
            String urlToBeFetched = "";
            try {
// get a new url to be fetched from the database
                urlToBeFetched = dbManager.getNextURLToBeFetched();

                if (urlToBeFetched != null && !"".equals(urlToBeFetched)) {
                    System.out.println("[" + myNumber + "] Fetcing URL ==> " + urlToBeFetched);

                    // fetch it and save in a file
                    String fileName = fetchAndSaveResource(urlToBeFetched);

                    // inform database that you are done
                    dbManager.setURLFetched(urlToBeFetched, fileName);
                    System.out.println("[" + myNumber + "] Url Fetched ==> " + urlToBeFetched);
                } else {
                    Thread.sleep(1000 * 60 * 5);
                }
            } catch (BlogCrawlingException e) {
                e.printStackTrace();
                dbManager.setBlogProcessingFailed(urlToBeFetched);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
    }

    private String fetchAndSaveResource(String urlToBeFetched) throws BlogCrawlingException {
        GetMethod method = new GetMethod(urlToBeFetched);
        File htmlFile;
        try {
            method.setRequestHeader(new Header(Constants.HEADER_USER_AGENT, Constants.USER_AGENT_VAL));
            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body and save it

            htmlFile = new File(dataFolder, Hashing.getHashValue(urlToBeFetched));
            if (!htmlFile.isFile()) htmlFile.createNewFile();

            OutputStream out = new FileOutputStream(htmlFile);

            InputStream in = method.getResponseBodyAsStream();
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new BlogCrawlingException(e);
        } finally {
            method.releaseConnection();

        }

        return htmlFile.getName();
    }
}
