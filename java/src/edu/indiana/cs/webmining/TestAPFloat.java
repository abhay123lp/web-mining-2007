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

package edu.indiana.cs.webmining;

import org.apfloat.Apfloat;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : Apr 24, 2007
 */
public class TestAPFloat {
    public static void main(String[] args) {
        TestAPFloat testAPFloat = new TestAPFloat();
        String s = testAPFloat.calculateProbability(23, 88, 12345, 67);
        System.out.println("s = " + s);
    }


    /**
     * This is the equation we gonna calculate inside this.
     * <p/>
     * P(Q) = 1 / [ (k_a/(n-2))^q * ( 1- (k_b/(n-2)))^(k_b-q)]
     *
     * @param k_a
     * @param k_b
     * @param n
     * @param q
     * @return
     */
    public String calculateProbability(int k_a, int k_b, int n, int q) {
        Apfloat apfloat = new Apfloat(3);

        Apfloat x = new Apfloat(k_a / (n - 2.0));
        Apfloat result_1 = new Apfloat(k_a / (n - 2.0));
        for (int i = 0; i < q - 1; i++) {
            result_1 = result_1.multiply(x);
        }

        Apfloat y = new Apfloat(1 - (k_b / (n - 2)));
        Apfloat result_2 = new Apfloat(1 - (k_b / (n - 2)));
        for (int i = 0; i < (k_b - q - 1); i++) {
            result_2 = result_2.multiply(x);
        }

        Apfloat result = result_1.multiply(result_2);

        return new Apfloat(1).divide(result).toString(true);
    }
}