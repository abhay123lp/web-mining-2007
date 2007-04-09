package spider.util;

import java.util.Hashtable;

/**
 * @author pant
 *         <p/>
 *         To change this generated comment edit the template variable "typecomment":
 *         Window>Preferences>Java>Templates.
 *         To enable and disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class Tester2 {

    public static void main(String[] args) {
        Hashtable ht = new Hashtable();
        ht.put("topical", new Integer(5));
        ht.put("machine", new Integer(2));
        ht.put("crawling", new Integer(100));
        System.out.println(Helper.getSimInQuerySpace("topical machine cawling", "topical machine crawling is a new area for research"));

        double[] a = {1, 0};
        double[] b = {1, 1};
        double cos = Helper.getCosine(a, b);
        System.out.println("Cosine:" + cos);

    }
}
