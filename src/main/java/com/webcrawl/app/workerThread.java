package com.webcrawl.app

import java.util.concurrent.BlockingQueue;

/**
 *
 *
 */

public class workerThread implements Runnable {

    //this is the queue of sites to be crawled
    private BlockingQueue<String> queue;

    //Constructor for class
    public workerThread(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    //implementing runnable requires run
    @Override
    public void run() {

    }


}