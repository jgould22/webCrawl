package com.webcrawl.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jordan on 28/11/16.
 * This class implements runnable to create a thread that handles the creation and management of workerThread threads
 */
public class webCrawler {

    //Hashmap to keep track of which documents have been crawled
    ConcurrentHashMap<String, String> visited = new ConcurrentHashMap<String, String>();
    int numThreads;
    //this is the queue of sites to be crawler
    private LinkedBlockingQueue<String> linkQueue = new LinkedBlockingQueue<String>();

    //creates threads and places seed urls in queue
    public webCrawler(int numThreads, Long maxGraphSize) {

        this.numThreads = numThreads;
        AtomicLong graphSize = new AtomicLong();

        try {
            linkQueue.put("http://www.csd.uwo.ca/~solis/");
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < numThreads; i++) {
            Runnable worker = new workerThread(linkQueue, graphSize, maxGraphSize);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {

        }

    }

    //


}
