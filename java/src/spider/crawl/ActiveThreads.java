package spider.crawl;

/**
 * keeping track of blog threads so that they can be stopped when none of them have further URLs to crawl
 *
 * @author Gautam Pant
 */
public class ActiveThreads {
    private int activeThreads = 0;

    /**
     * add to active thread count
     */
    public synchronized int add() {
        activeThreads++;
        return activeThreads;
    }

    /**
     * subtract from the current active threads count
     */
    public synchronized int subtract() {
        activeThreads--;
        return activeThreads;
    }

    /**
     * get the latest active threads count
     */
    public synchronized int get() {
        return activeThreads;
    }
}
