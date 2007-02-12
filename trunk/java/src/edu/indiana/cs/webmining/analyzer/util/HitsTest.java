package edu.indiana.cs.webmining.util;

public class HitsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HitsWrapper hw = new HitsWrapper();
		
		hw.addEdge("HubA1", "TopicA1");
		hw.addEdge("HubA2", "TopicA1");
		hw.addEdge("HubA3", "TopicA1");
		hw.addEdge("HubA4", "TopicA1");
		hw.addEdge("HubA4", "TopicA2");
		hw.addEdge("HubA5", "TopicA2");
		
		hw.analyze();
	}

}
