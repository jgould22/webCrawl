package com.webcrawl.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jordan on 28/11/16.
 * This class implements runnable to create a thread that handles the creation and management of workerThread threads
 */
public class webCrawler {

    private Comparator<siteNode> nodeCompare = new Comparator<siteNode>() {
        @Override
        public int compare(siteNode one, siteNode two) {
            return one.getDistanceFromRoot() - two.getDistanceFromRoot();
        }
    };

    //Number of crawling threads
    private int numThreads;
    //this is the queue of sites to be crawler, the "frontier" of the graph
    private PriorityBlockingQueue<siteNode> frontierQueue = new PriorityBlockingQueue<siteNode>(11, nodeCompare);
    //Graph size counter
    private AtomicLong graphSizeCounter = new AtomicLong();
    //maximum size of the graph
    private Long maxGraphSize;

    private LinkedBlockingQueue<siteNode> finishedQueue = new LinkedBlockingQueue<siteNode>();

    private urlFilter filter;

    //creates threads and places seed urls in queue
    public webCrawler(int numThreads, Long maxGraphSize) {

        this.numThreads = numThreads;
        this.maxGraphSize = maxGraphSize;
        this.filter = urlFilter.getInstance();

    }

    public void startCrawl(String seedUrl) {

        try {
            URL rootURL = new URL(seedUrl);

            siteNode rootNode = new siteNode(rootURL, null, 0);

            if (!filter.urlAlreadyVisited(rootURL)) {

                frontierQueue.put(rootNode);
                ExecutorService executor = Executors.newCachedThreadPool();

                for (int i = 0; i < numThreads; i++) {
                    Runnable worker = new workerThread(frontierQueue, finishedQueue, graphSizeCounter, maxGraphSize, filter);
                    executor.execute(worker);
                }

                int count = 0;

                while (true) {


                    try {
                        siteNode node = finishedQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    count += 1;
                    System.out.println(count);

                }

            }
        } catch (MalformedURLException e) {

            e.printStackTrace();

        }


    }


}
