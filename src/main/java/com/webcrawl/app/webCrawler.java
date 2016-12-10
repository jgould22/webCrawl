package com.webcrawl.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jordan on 28/11/16.
 * This class implements runnable to create a thread that handles the creation and management of workerThread threads
 */
public class webCrawler {

    //Hashmap to keep track of which documents have been crawled as well as documents that should
    private ConcurrentHashMap<URL, siteNode> graph;
    //Number of crawling threads
    private int numThreads;
    //this is the queue of sites to be crawler, the "frontier" of the graph
    private PriorityBlockingQueue<siteNode> frontierQueue;
    //Graph size counter
    private AtomicLong graphSize;
    //maximum size of the graph
    private Long maxGraphSize;
    //The graph root
    private siteNode root;
    //HashSet of hosted visted
    private HashSet<URL> hostsVistied;
    //nodesVisite Hashset of visited nodes
    private HashSet<URL> nodesVisited;


    private Comparator<siteNode> nodeCompare = new Comparator<siteNode>() {
        @Override
        public int compare(siteNode one, siteNode two) {
            return one.getDistanceFromRoot() - two.getDistanceFromRoot();
        }
    };


    //creates threads and places seed urls in queue
    public webCrawler(int numThreads, Long maxGraphSize) {

        this.numThreads = numThreads;
        this.maxGraphSize = maxGraphSize;
        this.frontierQueue = new PriorityBlockingQueue<siteNode>(11, nodeCompare);
        this.graph = new ConcurrentHashMap<URL, siteNode>();
        this.hostsVistied = new HashSet<URL>();
        this.graphSize = new AtomicLong();

    }

    public void startCrawl(String seedUrl) {

        //create root Node and add to frontier
        try {
            //Create new root node
            this.root = new siteNode(new URL(seedUrl), null, 0);
            //add root node to the frontier
            frontierQueue.put(this.root);
            graphSize.incrementAndGet();
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        ThreadPoolExecutor executor;
        executor = new ThreadPoolExecutor(5, numThreads, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        //Increment for each loop until max graph size is met
        while (graphSize.getAndIncrement() <= maxGraphSize) {

            //take from frontier and submit to thread poo
            //                 //take urlString from frontierQueue
            try {

                siteNode node = frontierQueue.take();
                if (!hostsVistied.contains(node.getURL())) {

                    robotTxtHandler(node.getURL());

                }

                Runnable worker = new workerThread(node, frontierQueue, graph);
                executor.submit(worker);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // Tell threads to finish off since graph is now max size
        System.out.println("Done crawling, waiting for threads to finish");

        executor.shutdown();
        try {
            while (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Finished ");


        //print all the nodes in teh graph
        for (URL url : graph.keySet()) {

            System.out.println(url.toString());

        }

    }

    private void robotTxtHandler(URL url) {

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new URL("http://" + url.getHost() + "/robots.txt").openStream()))) {
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
