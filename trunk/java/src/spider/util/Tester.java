package spider.util;

/**
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 *
 * @author Gautam Pant
 */
public class Tester {

    public static void main(String[] args) {
        //double sim = Helper.getSim("gautam pant","gautam pant gautam pant gautam pant pant gautam ya ya");
        //System.out.println(sim);
        try {
            String x = "Okay";
            System.out.println(x.toLowerCase());
            System.out.println(x);
            //String url1 = "http://www.Spctroscopynow.com:9010/Spy/basehtml/SpyH/1,2466,0-4-16272-0-16272-directories--0,00.html";
            //String url2= "http://www.Spectroscopynow.com:9010/Spy/basehtml/SpyH/1,2466,0-4-16272-0-16272-directories--0,00.htm";
            /*String url = "http://www.cerrocoso.edu/#content";
               //System.out.println(Hashing.getHashValue(url));*/
            String url = "http://exelixis.com/discovery/pr_1010092191";
            System.out.println(Helper.getDomainName(url));
            /*String res =Helper.getCanonical(url);
               System.out.println("URL:"+res);
               System.exit(1);
               String ans = (new URL(new URL(url),res)).toString();
               //System.out.println(Helper.getHostNameWithPort(url1));
               Page pg = (new Fetcher()).fetch(res);

               //System.out.println(pg.content);
               //Vector v = RobotExclusion.getVector(pg.content);
               HTMLParser hp = new HTMLParser();
               String out = hp.htmlToXML(pg.content, url);

               try{
                   FileWriter fw = new FileWriter("temp.txt");
                   fw.write(out);
                   fw.close();
               }catch (Exception e){
                   e.printStackTrace();
               }
               File f = new File("temp.txt");
               //System.out.println(out);
               //System.out.println(Helper.getLinkContextWords(out, 4));
               XMLParser p = new XMLParser(f);
               System.out.println(p.getContents());
               //Document doc = null;
               if (p.startParser()){
                   String[] links = p.getLinks();
                   if (links != null){
                       for(int j = 0; j < links.length; j++){
                               //System.out.println(j+links[j]);
                       }
                   }

                   //String text = p.getText();
                   //System.out.println(text);
                   Hashtable ht = p.getLinkContextAdaptive(2);
                   System.out.println(ht);
               }*/
            /*System.out.println(Hashing.getHashValue(url1));
               System.out.println(Hashing.getHashValue(url2));*/
            //System.out.println(Helper.getCanonical(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
