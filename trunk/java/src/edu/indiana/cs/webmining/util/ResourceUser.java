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

package edu.indiana.cs.webmining.util;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * ResourceUser
 * <p>This abstract class provides a built-in method 'use' that takes
 * a generic resource.</p>
 * 
 * <p>Users who extend this class is required to implement a run method
 * that can return an exception, use the resource variable 'rsrc'
 * and save the result in 'result'.</p>
 * 
 * <p>An instance of this class is used by invoking 'use'; use will invoke the run
 * method and return the content of the 'result' variable, taking care of closing
 * the resource if necessary.</p>
 * 
 * @author Michel Salim <msalim@cs.indiana.edu>
 *
 * @param <R> The class name of the resource used
 * @param <T> The return type of the run method of the class implementing this interface
 * @param <E> The exception class associated with the resource
 */

public abstract class ResourceUser<R, T, E extends Exception> {
    protected R rsrc;
    protected T result;
    
    public T use(R resource) throws E {
        this.rsrc = resource;
        try {
            this.run();
        } finally {
            if (rsrc != null &&
                    // !(rsrc instanceof PreparedStatement) &&
                    (rsrc instanceof Statement)) {
                try {
                    ((Statement)rsrc).close();
                } catch (SQLException e) {
                    System.err.println("Failed to close statement");
                }
            }
        }
        return result;
    }
    public abstract void run() throws E;
}

