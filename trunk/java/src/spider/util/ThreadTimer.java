package spider.util;

/**
 * Stops a thread after specified time
 *
 * @author Gautam Pant
 */
public class ThreadTimer extends Thread {

    Thread t = null;
    long time = 0;

    public ThreadTimer(Thread t, long time) {
        this.t = t;
        this.time = time;
    }

    /**
     *
     */
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            if (t == null) {
                return;
            }
            if (!t.isAlive()) {
                return;
            }
            while ((System.currentTimeMillis() - startTime) < time) {
                Thread.sleep(10);
                if (!t.isAlive()) {
                    return;
                }
            }
            if (t.isAlive()) {
                t.interrupt();
                t.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
