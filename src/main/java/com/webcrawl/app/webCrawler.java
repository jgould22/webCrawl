package com.webcrawl.app;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.TarjanStronglyConnectedComponents;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    private AtomicInteger blockingThreads;

    private Graph graph;

    private APSP apsp;

    private TarjanStronglyConnectedComponents tscc;

    private Toolkit toolkit;

    //creates threads and places seed urls in queue
    public webCrawler(int numThreads, Long maxGraphSize) {

        this.numThreads = numThreads;
        this.maxGraphSize = maxGraphSize;
        this.filter = urlFilter.getInstance();
        this.blockingThreads = new AtomicInteger(numThreads);
        this.graph = new SingleGraph("webGraph");
        this.graph.setStrict(false);
        this.graph.setAutoCreate(true);
        this.apsp = new APSP();
        tscc = new TarjanStronglyConnectedComponents();
        this.apsp.init(graph); // registering apsp as a sink for the graph
        tscc.init(graph);
        this.apsp.setDirected(true);
        tscc = new TarjanStronglyConnectedComponents();
        tscc.init(graph);
        this.toolkit = new Toolkit();

    }

    public void startCrawl(String seedUrl) {

        System.out.println("Starting Crawl");
        try {
            URL rootURL = new URL(seedUrl);

            siteNode rootNode = new siteNode(rootURL, null, 0);

                frontierQueue.put(rootNode);

                ExecutorService executor = Executors.newCachedThreadPool();

                for (int i = 0; i < numThreads; i++) {
                    Runnable worker = new workerThread(frontierQueue, finishedQueue, graphSizeCounter, maxGraphSize, filter, blockingThreads);
                    executor.execute(worker);
                }


            int count = 0;

                while (true) {

                    try {

                        siteNode node = finishedQueue.poll(1, TimeUnit.SECONDS);

                        if (node != null) {
                            insertNode(node);
                            count++;
                        }

                        //if the graph size has been reached or there are no active threads and the finished queue is empty break
                        if ((node == null && blockingThreads.get() == 0 && frontierQueue.isEmpty() && finishedQueue.isEmpty())
                                || count >= maxGraphSize) {
                            break;
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            //Shut down the threads
            executor.shutdownNow();

            printGraphStats();
            graph.display();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        }


    }

    private void insertNode(siteNode node) {

        LinkedList<URL> edges = node.getOutGoingEdges();

        for (URL edge : edges) {

            try {
                graph.addEdge(node.getURL().toString() + " " + edge.toString(), node.getURL().toString(), edge.toString(), true);
            } catch (IdAlreadyInUseException e) {


            }

        }


    }

    private void printGraphStats() {

        //Computer APSP using FW
        //apsp.compute();

        int[] distribution = Toolkit.degreeDistribution(graph);

        System.out.println("Edge Distribution");
        System.out.println("Number of Edges Edges/Number of Nodes");
        for (int i = 1; i < distribution.length; i++) {

            if (distribution[i] != 0)
                System.out.println(i + "\t\t\t\t\t\t" + distribution[i]);

        }
        System.out.println("Average Edge Degree: " + Toolkit.averageDegree(graph));


        //Computer Strongly connected components with Tarjans
        System.out.println("Computing Strongly Connected Components");
        tscc.compute();
        //print number of components
        HashSet<Integer> numComponents = new HashSet<Integer>();
        System.out.println("Number of Strongly Connected Components");
        for (Node n : graph.getEachNode())
            numComponents.add(n.getAttribute("scc"));
        System.out.println(numComponents.size());


        //Computer APSP using FW
        System.out.println("Computing Average Path Length(This can take a while");
        apsp.compute();
        long totalDistance = 0;
        long totalPaths = 0;
        for (Node n : graph.getEachNode()) {
            APSPInfo info = n.getAttribute(APSPInfo.ATTRIBUTE_NAME);
            info.targets.forEach((key, value) -> totalDistance += value.distance);)
            totalPaths++;
        }

        System.out.println("Average Shortest Path Length: " + totalDistance / totalPaths);


    }


}
