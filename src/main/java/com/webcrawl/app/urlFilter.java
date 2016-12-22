package com.webcrawl.app;


import java.net.URL;
import java.util.HashSet;

/**
 * Created by jordan on 16/12/16.
 * This is a simple singleton to handle URL filtering
 * This class will filter already visited URLs as well as robot.txt files
 */

public class urlFilter {

    private static urlFilter instance = null;
    //HashSet of hosted visted
    private HashSet<String> hostsVistied;
    //nodesVisite Hashset of visited nodes
    private HashSet<URL> nodesVisited;

    protected urlFilter() {
        // Exists only to defeat instantiation.
    }

    public static urlFilter getInstance() {
        if (instance == null) {
            instance = new urlFilter();
            instance.hostsVistied = new HashSet<String>();
            instance.nodesVisited = new HashSet<URL>();
        }
        return instance;
    }

    //Returns true if url has already been visited
    public synchronized boolean urlAlreadyVisited(URL url) {

        return nodesVisited.contains(url);


    }

    public synchronized void addURL(URL url) {

        nodesVisited.add(url);

    }

}