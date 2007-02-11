package edu.indiana.cs.webmining.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class NetworkGenerator {

    private Connection conn;
    private Random rng;
    private ArrayList<Topic> topics;
    private HashMap<String, ArrayList<String>> topicAuthorities;
    private HashMap<String, ArrayList<String>> topicHubs;

    class Topic {
        public String name;
        public double prob;

        public Topic(String name, double prob) {
            this.name = name;
            this.prob = prob;
        }
    }

    private void initialize() {
        try {
            conn = DBEngine.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rng = new Random();
        topics = new ArrayList<Topic>();
        topicAuthorities = new HashMap<String, ArrayList<String>>();
        topicHubs = new HashMap<String, ArrayList<String>>();
    }

    public void addTopic(String topic, double prob) {
        topics.add(new Topic(topic, prob));
        topicAuthorities.put(topic, new ArrayList<String>());
    }

    public String pickTopic() {
        double roll = rng.nextDouble();
        double limit = 0.0;
        String res = "";
        for (Topic t : topics) {
            limit = limit + t.prob;
            if (roll <= limit) {
                res = t.name;
                return res;
            }
        }
        return res;
    }

    public void addAuthority(String topic) {
        ArrayList<String> auths = topicAuthorities.get(topic);
        String nodeName = topic + "Auth" + auths.size();
        auths.add(nodeName);
    }

    public void createAuthorities(int n) {
        for (int i = 0; i < n; ++i) {
            addAuthority(pickTopic());
        }
    }

    public void addHub(String topic) {
        ArrayList<String> hubs = topicHubs.get(topic);
        String nodeName = topic + "Hub" + hubs.size();
        // want hub's outdegree to be normally distributed
        // and have a 'noise ratio' percentage of being off-topic

    }

    public NetworkGenerator() {
        initialize();

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new NetworkGenerator();
    }

}
