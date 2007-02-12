package edu.indiana.cs.webmining.util;

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
