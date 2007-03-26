/**
 * Copyright (C) 2007 The Trustees of Indiana University. All rights reserved.
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

package edu.indiana.cs.webmining.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Michel Salim <msalim@cs.indiana.edu>
 * @since Feb 10, 2007
 */
public class DBInitializer {

    private static Connection conn;

 

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        BufferedReader reader;

        if (args.length < 1) {
            System.err.println("Usage: java DBInitializer filename");
            return;
        }

        try {
            reader = new BufferedReader(new FileReader(args[0]));

            try {
                conn = DBManager.getConnection();
                int count = 0;
                String[] tokens;
                String line = reader.readLine();
                while (line != null) {
                    ++count;
                    if ((count % 1000) == 0) {
                        System.out.println("Added " + count + " links");
                    }
                    tokens = line.split("[\\s,]+");
                    try {
                        DBManager.addLink(tokens[1], tokens[0]);
                    } catch (MalformedURLException e) {
                        System.err.println("Malformed URL: " + e.getMessage());
                    }
//					System.out.println("|" + src + "|" + dest + "|");
                    line = reader.readLine();
                }
                reader.readLine();
                conn.close();
            } catch (SQLException e) {
                System.err.println("Failed to open database connection");
                e.printStackTrace();
                return;
            }

            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            return;
        } catch (IOException e) {
            System.err.println("Cannot perform IO operation");
            return;
        }
        System.out.println("Database populated");
	}
}
