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
import java.util.concurrent.atomic.AtomicInteger;
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
    private AtomicInteger blockingThreads;
    //Constructor for class
    public workerThread(PriorityBlockingQueue<siteNode> frontierQueue,
                        LinkedBlockingQueue<siteNode> finishedQueue,
                        AtomicLong graphCounter,
                        Long maxGraphSize,
                        urlFilter filter,
                        AtomicInteger blockingThreads) {

        this.frontierQueue = frontierQueue;
        this.finishedQueue = finishedQueue;
        this.graphCounter = graphCounter;
        this.maxGraphSize = maxGraphSize;
        this.filter = filter;
        this.blockingThreads = blockingThreads;

    }

    @Override
    public void run() {

        while (true) {

            try {

                //Increment counter so graph thread is aware of blocking
                blockingThreads.getAndDecrement();
                //
                siteNode node = frontierQueue.take();
                //Decrement because thread is no longer blocking
                blockingThreads.getAndIncrement();

                //Download page
                String page = downaloadPage(node.getURL());
                //Parse it for links
                node = parsePage(node, page);

                filter.addURL(node.getURL());

                LinkedList<URL> edges = node.getOutGoingEdges();

                for (URL edge : edges) {

                    siteNode newNode = new siteNode(edge, node.getURL(), node.getDistanceFromRoot() + 1);

                    if (graphCounter.get() <= maxGraphSize && !filter.urlAlreadyVisited(edge)) {
                        graphCounter.getAndIncrement();
                        frontierQueue.put(newNode);
                    } else {
                        break;
                    }

                }

                finishedQueue.put(node);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                return;

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
        } catch (IOException ioe) {
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
            } catch (MalformedURLException e) {
            } catch (IllegalArgumentException e) {
            }

        }

        return node;

    }


}
