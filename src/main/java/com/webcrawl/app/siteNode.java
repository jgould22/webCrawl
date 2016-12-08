package com.webcrawl.app;

import java.net.URL;
import java.util.LinkedList;

/**
 * Created by jordan on 06/12/16.
 */
public class siteNode {
    private URL host;
    private LinkedList<String> edges;

    public siteNode(URL hostAddress) {
        this.host = hostAddress;
        this.edges = new LinkedList<String>();
    }

    public void addEdge(String s) {

        edges.add(s);

    }

    public LinkedList getEdges() {
        return edges;
    }

}
