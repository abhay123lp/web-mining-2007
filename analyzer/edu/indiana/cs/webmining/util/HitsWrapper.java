package edu.indiana.cs.webmining.util;

import java.util.HashMap;

import Jama.Matrix;

public class HitsWrapper {
	
	private HashMap<String, Integer> V;
	private HashMap<Integer, String> Vinv;
	private int lastidx;
	
	private HashMap<Integer, HashMap<Integer, Double>> adj;
	
	public HitsWrapper(){
		initialize();
	}
	
	private void initialize() {
		V = new HashMap<String, Integer>();
		Vinv = new HashMap<Integer, String>();
		lastidx = 0;
		adj = new HashMap<Integer, HashMap<Integer, Double>>();
		
	}
	public int addVertex(String v1) {
		if (V.containsKey(v1)) {
			return (Integer)(V.get(v1));
		} else {
			V.put(v1, lastidx);
			Vinv.put(lastidx, v1);
			adj.put(lastidx, new HashMap<Integer, Double>());
			return lastidx++;
		}
	}
	public void addEdge(String v1, String v2) {
		int i1 = addVertex(v1);
		int i2 = addVertex(v2);
		adj.get(i1).put(i2, 1.0);
		return;
	}
	
	private double[][] converter(HashMap<Integer, HashMap<Integer, Double>> in) {
		int n = in.size();
		double[][] out = new double[n][n];
		for (int i=0; i<n; i++) {
			for (int j=0; j<n; j++) {
				if (in.get(i).containsKey(j)) {
					out[i][j] = 1.0;
				} else {
					out[i][j] = 0.0;
				}
			}
		}
		return out;
	}

	public void analyze() {
		Matrix adjMat = new Matrix(converter(adj));
		adjMat.print(4, 2);
		HITS h = new HITS();
		h.setGraph(adjMat);
		double[] auths = h.getAuthorities();
		double[] hubs = h.getHubs();
		
		System.out.println("Node\t\tAuth\t\tHub");
		for (int i=0; i < auths.length; ++i) {
			System.out.print(Vinv.get(i));
			System.out.print(":\t");
			System.out.print(auths[i] + "\t");
			System.out.println(hubs[i]);
		}
	}
}
