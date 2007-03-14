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

package edu.indiana.cs.webmining.blog.impl;

import edu.indiana.cs.webmining.blog.BlogDataStorage;
import spider.util.Hashing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Mar 8, 2007
 */
public class FileBasedBlogDataStorage implements BlogDataStorage {

    private File storageFolder;


    public FileBasedBlogDataStorage() {
        this("BlogData");
    }

    public FileBasedBlogDataStorage(String storageFolderLocation) {
        this.storageFolder = new File(storageFolderLocation);
        if (!this.storageFolder.isDirectory()) {
            storageFolder.mkdir();
        }
    }

    public void store(String[] destinationURLs, String sourceBlog) {

        try {
// I am using a file which has a name by hashing the url of the source blog
            String fileName = Hashing.getHashValue(sourceBlog) + ".txt";
            File file = new File(storageFolder, fileName);
            BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
//            if (!file.isFile()) {
//                file.createNewFile();
//                out.write("*********************************************************************************\n");
//                out.write(" Source Blog = " + sourceBlog + "\n");
//                out.write("**********************************************************************************\n");
//            }

            if (file.length() == 0) {
                out.write("*********************************************************************************\n");
                out.write(" Source Blog = " + sourceBlog + "\n");
                out.write("**********************************************************************************\n");
            }

            for (String destinationURL : destinationURLs) {
                out.write(destinationURL + "\n");
            }

            out.close();

        } catch (FileNotFoundException e) {
            // I don't think this error will occur. But leaving the stacktrace, in case if something
            // goes wrong
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void writeFileHeader(File file, String sourceBlog) {

    }
}
