package com.webcrawl.app;

import java.net.URL;
import java.util.LinkedList;

/**
 * Created by jordan on 06/12/16.
 */
public class siteNode {
    private URL url;
    private LinkedList<URL> outGoingEdges;
    private LinkedList<URL> incomingEdges;
    private int distanceFromRoot;

    //for poison pill
    public siteNode() {
        this.url = null;
        this.outGoingEdges = null;
        this.incomingEdges = null;
    }


    public siteNode(URL hostAddress, URL parentNode, int distanceFromRoot) {
        this.url = hostAddress;
        this.outGoingEdges = new LinkedList<URL>();
        this.incomingEdges = new LinkedList<URL>();
        if (parentNode != null) {
            incomingEdges.add(parentNode);
        }
        this.distanceFromRoot = distanceFromRoot;
    }

    public URL getURL() {
        return this.url;
    }

    public boolean addOutGoingEdge(URL s) {

        if (outGoingEdges.contains(s))
            return true;
        else {
            outGoingEdges.add(s);
            return true;
        }

    }


    public boolean addIncomingEdges(URL s) {

        incomingEdges.add(s);
        return true;

    }


    public LinkedList getOutGoingEdges() {
        return outGoingEdges;
    }

    public int getDistanceFromRoot() {
        return distanceFromRoot;
    }

}
