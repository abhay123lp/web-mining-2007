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


package edu.indiana.cs.webmining.analyzer.util;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class HITS {
	private Matrix adj;
	
	public HITS() {
		initialize();
	}
	
	private void initialize() {
		
	}
	
	private int indexOfMaxD(Matrix diag) {
		int curi = diag.getColumnDimension()-1;
		double curval = diag.get(curi, curi);
		for (int i = curi-1; i >= 0; --i) {
			if (diag.get(i, i) > curval) {
				curi = i;
				curval = diag.get(i, i);
			}
		}
		return curi;
	}
	private double[] getColumn(Matrix m, int colidx) {
		int dim = m.getRowDimension();
		double[] ans = new double[dim];
		for (int i = 0; i < dim; ++i) {
			ans[i] = m.get(i, colidx);
		}
		return ans;
	}
	public double[] principalEigenvector(Matrix input) {
		input.print(4, 2);
		EigenvalueDecomposition eig = input.eig();
		Matrix D = eig.getD();
		//D.print(4, 2);
		eig.getV().print(4,2);
		return getColumn(eig.getV(), indexOfMaxD(D));
	}

	public void setGraph(Matrix adj) {
		this.adj = adj;
	}
	
	public double[] getAuthorities() {
		System.out.println("Calculating authorities");
		return principalEigenvector(adj.transpose().times(adj));
	}
	public double[] getHubs() {
		System.out.println("Calculating hub scores");
		return principalEigenvector(adj.times(adj.transpose()));
	}
	
}
