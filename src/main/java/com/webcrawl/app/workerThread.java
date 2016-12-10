package com.webcrawl.app;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;


/**
 *
 *
 */

public class workerThread implements Runnable {

    //this is the frontierQueue of sites to be crawled
    private PriorityBlockingQueue<siteNode> frontierQueue;
    //Set of visited nodes so no node is visited twice
    private ConcurrentHashMap.KeySetView<URL, siteNode> graph;
    //the node for this thread to crawl
    private siteNode node;
    private HashSet visited;


    //Constructor for class
    public workerThread(siteNode node,
                        PriorityBlockingQueue<siteNode> frontierQueue,
                        ConcurrentHashMap<URL, siteNode> graph) {

        this.frontierQueue = frontierQueue;
        this.graph = graph.keySet();
        this.node = node;

    }

    @Override
    public void run() {


        graph.getMap().put(node.getURL(), node);
        //Download page
        String page = downaloadPage(node.getURL());
        //Parse it for links
        node = parsePage(node, page);


        //validate the edges
        LinkedList<URL> edges = node.getOutGoingEdges();

        for (URL edge : edges) {

            //check if it is visited
            siteNode newNode = new siteNode(edge, node.getURL(), node.getDistanceFromRoot() + 1);
            if (alreadyVisitedEdge(edge)) {
                graph.getMap().get(edge).addIncomingEdges(node.getURL());
            } else {
                frontierQueue.add(newNode);
            }

        }
        //add the node to the graph

        //work done shutdown and exit thread
        Thread.currentThread().interrupt();
        return;

    }

    private String downaloadPage(URL url) {

        BufferedReader bis;
        String s;
        String page = "";

        try {

	        /* URL that the web crawler will download. You might change this
             URL to download other pages                                   */
            bis = new BufferedReader(new InputStreamReader(url.openStream()));

            while ((s = bis.readLine()) != null) {
                page += s;
            }
            bis.close();
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return page;
    }

    private siteNode parsePage(siteNode node, String page) {
        //Parse the page for links
        Document doc = Jsoup.parse(page);
        //Elements contains and the href and a tags contents
        Elements links = doc.select("a[abs:href]");

        //Loop through the links
        for (Element link : links) {

            try {

                URI newURI = new URI(link.attr("abs:href"));

                //check if its absolute link
                if (!newURI.isAbsolute()) {
                    newURI.resolve(node.getURL().toURI());
                }

                URL newURL = newURI.toURL();

                node.addOutGoingEdge(newURL);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }

        return node;

    }

    /*
    This class contains the rules for adding the edge to the graph, returns true if edge can be added
     */
    private boolean validateEdge(URL url) {

        //check that it has already been visited
        if (alreadyVisitedEdge(url)) {
            System.out.println("Already Crawled " + url.toString());
            return false;
        }

        //no exclusion rules apply so return true to add the edge
        return true;

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Below are the URL validation rules, if one of the results returns true, the edge is not added  //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean alreadyVisitedEdge(URL edgeURL) {

        return graph.contains(edgeURL);

    }

    private void addRobots(URL url) {

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new URL("http://google.com/robots.txt").openStream()))) {
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

/*



                //Check Edge Validity and add it to the node
                if (validateEdge(newURL)) {
                    //add edge to node since it is valid
                 //   System.out.println(newURL.toString() + " Added to Frontier");
                    //Add edge to node

                }




                for (URL edge : edges) {

                    try {
                        //transfer this to main crawl loop
                        frontierQueue.put(edge);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

 */


