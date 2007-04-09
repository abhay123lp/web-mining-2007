package spider.crawl;

import spider.util.Helper;

import java.io.File;
import java.io.FileWriter;

/**
 * maintain statistics related with crawl
 * 1. no of timeouts
 * 2. no of redirections
 * 3. no of parsing errors
 * 4. time since start
 * 5. number of threads
 * 6. size of frontier
 * 7. Memory in use
 * 8. Available memory
 * 9. no of pages fetched with 200 OK
 * 10. no of pages crawled
 * the statistics can be started as a thread and it prints out a file every t minutes
 *
 * @author Gautam Pant
 */
public class Statistics extends Thread {

    private int t = 60; //print the new statistics file every 60 seconds
    private String file = "statistics.txt"; //file written out with statistics
    private boolean stop = false; //to stop writing to file;

    //variables to be measured
    private long noTimeouts = 0;
    private long noRedirections = 0;
    private long noOkay = 0;
    private long noNotFound = 0;
    private long noThreads = 0;
    private long noParseErrors = 0;
    private long memoryInUse = 0;
    private long memoryAvailable = 0;
    private long noPagesCrawled = 0;
    private long noFoundInCache = 0;
    private long startTime = 0;
    private History h = null;
    private Frontier front = null;

    public Statistics(long start, History h, Frontier front) {
        startTime = start;
        this.h = h;
        this.front = front;
    }

    public Statistics() {
        startTime = System.currentTimeMillis();
    }

    public void run() {
        long lastTime = 0;
        while (!stop) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTime) / 1000 >= t) {
                lastTime = currentTime;
                toFile();
            }
        }
    }

    public synchronized void toFile() {
        //write to file
        try {
            FileWriter f = new FileWriter(file);
            f.write("Start Time: " + startTime + "\n");
            f.write("Time since start: " + ((System.currentTimeMillis() - startTime) / (1000)) + " seconds\n");
            if (h != null)
                f.write("Pages crawled: " + h.size() + "\n");
            f.write("Pages with response code 200: " + noOkay + "\n");
            f.write("Pages timed out: " + noTimeouts + "\n");
            f.write("Pages not found:  " + noNotFound + "\n");
            f.write("Found in cache: " + noFoundInCache + "\n");
            f.write("Parsing errors: " + noParseErrors + "\n");
            if (front != null)
                f.write("Frontier size: " + front.size() + "\n");
            f.flush();
            f.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the file.
     *
     * @param file The file to set
     */
    public void setFile(String file) {
        String[] parts = file.split("\\" + File.separatorChar);
        if (parts.length > 1) {
            String path = Helper.join(File.separatorChar + "", parts, 0, parts.length - 2);
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
        this.file = file;
    }

    /**
     * Sets the stop.
     *
     * @param stop The stop to set
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    /**
     * add to noTimeouts
     */
    public synchronized void addTimeouts(int k) {
        noTimeouts = noTimeouts + k;
    }

    /**
     * add to noOkay
     */
    public synchronized void addOkay(int k) {
        noOkay = noOkay + k;
    }

    /**
     * add to noNotFound
     */
    public synchronized void notFound(int k) {
        noNotFound = noNotFound + k;
    }

    /**
     * add to noParseErrors
     */
    public synchronized void parseErrors(int k) {
        noParseErrors = noParseErrors + k;
    }

    /**
     * add to noFoundInCache
     */
    public synchronized void foundInCache(int k) {
        noFoundInCache = noFoundInCache + k;
    }


    /**
     * pages crawled
     */
    public synchronized void pagesCrawled(long p) {
        noPagesCrawled = p;
	}
	
}
