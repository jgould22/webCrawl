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
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 *
 */

public class workerThread implements Runnable {

    AtomicLong nodesCrawled = new AtomicLong();
    Long maxGraphSize = new Long(0);
    //this is the queue of sites to be crawled
    private LinkedBlockingQueue<String> queue;

    //Constructor for class
    public workerThread(LinkedBlockingQueue<String> queue, AtomicLong nodesCrawled, Long maxGraphSize) {
        this.queue = queue;
        this.nodesCrawled = nodesCrawled;
        this.maxGraphSize = maxGraphSize;
    }

    @Override
    public void run() {

        while (true) {

            Long loopTest = nodesCrawled.getAndIncrement();
            System.out.println(loopTest);
            if (loopTest >= maxGraphSize) {
                Thread.currentThread().interrupt();
                return;
            }

            try {
                //take urlString from queue
                String s = queue.take();
                //Cast to URL object
                URL u = new URL(s);
                //Download page
                String page = downaloadPage(u);
                //Parse it for links
                siteNode node = parsePage(u, page);
                //add links to frontier queue
                LinkedList<String> edges = node.getEdges();
                for (String edge : edges) {

                    try {
                        queue.put(edge);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
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

    private siteNode parsePage(URL u, String page) {
        //Parse the page for links
        Document doc = Jsoup.parse(page);
        //Elements contains and the href and a tags contents
        Elements links = doc.select("a[abs:href]");
        //Declare new Node
        siteNode node = new siteNode(u);

        //Loop through the links
        try {
            for (Element link : links) {

                URI newURI = new URI(link.attr("abs:href"));
                if (newURI.isAbsolute()) {
                    node.addEdge(link.attr("abs:href"));
                } else {
                    newURI.resolve(u.toURI());
                    node.addEdge(newURI.toString());
                }
                System.out.println(link.attr("abs:href"));

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return node;

    }


}


