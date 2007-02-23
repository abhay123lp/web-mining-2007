/**
 *  Copyright (C) 2007 The Trustees of Indiana University. All rights reserved.
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

package edu.indiana.cs.webmining.util;

import java.sql.Statement;

/**
 * The Using class provides implementations of the 'Loan pattern', similar to
 * the 'using' keyword in C# and Python 2.5.
 *
 * @author Michel Salim (msalim@cs.indiana.edu)
 */
public class Using {
    /**
     * This implementation of using is for use with an SQL statement
     *
     * @param <T>  The return type of the run method of the ResourceUser object
     * @param user The ResourceUser object that uses the statement
     * @return The result of invoking user.run
     * @throws java.sql.SQLException
     */
    @SuppressWarnings("unchecked")
    public static <R extends Statement, T, E extends Exception> T using(
            R rsrc,
            ResourceUser<R, T, E> user)
            throws E {
        T result = null;
        String errMsg = "using: ";
        try {
            result = user.run(rsrc);
        } catch (Exception e) {
            errMsg = errMsg + e.getMessage();
        }
        finally {
            try {
                if (rsrc != null)
                    rsrc.close();
            } catch (Exception e) {
                // Ideally we'd like to catch only exceptions of type E
                // but type erasure gets in the way
                Exception myE = new Exception(errMsg
                        + "\nCannot close statement: "
                        + e.getMessage());
                // Java type erasure is broken
                throw (E) myE;
            }
        }
        return result;
    }

}
