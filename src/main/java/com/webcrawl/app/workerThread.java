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
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 *
 */

public class workerThread implements Runnable {

    private static final siteNode POISON_PILL = new siteNode();
    //this is the frontierQueue of sites to be crawled
    private PriorityBlockingQueue<siteNode> frontierQueue;
    //the node for this thread to crawl
    private LinkedBlockingQueue<siteNode> finishedQueue;
    private AtomicLong graphCounter;
    private Long maxGraphSize;
    private urlFilter filter;
    //Constructor for class
    public workerThread(PriorityBlockingQueue<siteNode> frontierQueue,
                        LinkedBlockingQueue<siteNode> finishedQueue,
                        AtomicLong graphCounter,
                        Long maxGraphSize,
                        urlFilter filter) {

        this.frontierQueue = frontierQueue;
        this.finishedQueue = finishedQueue;
        this.graphCounter = graphCounter;
        this.maxGraphSize = maxGraphSize;
        this.filter = filter;

    }

    @Override
    public void run() {

        while (true) {
            try {

                siteNode node = frontierQueue.take();

                if (node == POISON_PILL) {
                    frontierQueue.add(POISON_PILL);
                    Thread.currentThread().interrupt();
                    return;
                }
                //Download page
                String page = downaloadPage(node.getURL());
                //Parse it for links
                node = parsePage(node, page);

                LinkedList<URL> edges = node.getOutGoingEdges();

                for (URL edge : edges) {

                    //check if it is visited
                    siteNode newNode = new siteNode(edge, node.getURL(), node.getDistanceFromRoot() + 1);
                    if (!filter.urlAlreadyVisited(edge)) {
                        //Increment the counter so graph remains proper size
                        if (graphCounter.getAndIncrement() <= maxGraphSize)
                            frontierQueue.add(newNode);
                        else {   //graph size has been reached, insert pill to shut down threads
                            frontierQueue.add(POISON_PILL);
                            break;
                        }

                    }

                }

                //return the node
                finishedQueue.put(node);


            } catch (InterruptedException e) {

                e.printStackTrace();

            }
        }

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


}
