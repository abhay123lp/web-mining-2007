package edu.indiana.cs.webmining.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;
import org.htmlparser.Parser;
import org.htmlparser.parserapplications.StringExtractor;

import edu.udo.cs.wvtool.main.WVTool;


public class AnalyzeDoc {
    HtmlCleaner cleaner;
    Parser parser;
    StringExtractor extractor;

    public AnalyzeDoc(String filename) throws IOException {
        cleaner = new HtmlCleaner(new File(filename));
        cleaner.clean();
        OutputStream out = new ByteArrayOutputStream();
        cleaner.writeCompactXmlToStream(out);

        String contents = out.toString();
        Pattern p = Pattern.compile("<[^>]*>");
        Matcher m = p.matcher(contents);

        String words = m.replaceAll(" ");
        WVTool wvtool = new WVTool(true);
        
        System.out.println(m.replaceAll(" "));


    }
    public static void main(String[] args) {
        try {
            new AnalyzeDoc(args[0]);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
