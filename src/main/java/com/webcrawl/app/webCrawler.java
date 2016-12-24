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
import java.util.Map;
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

    //Number of crawling threadsNumberFormatException
    private int numThreads;
    //this is the queue of sites to be crawler, the "frontier" of the graph
    private PriorityBlockingQueue<siteNode> frontierQueue = new PriorityBlockingQueue<siteNode>(11, nodeCompare);
    //Graph size counter
    private AtomicLong graphSizeCounter = new AtomicLong();
    //maximum size of the graph
    private Long maxGraphSize;
    //Stores the edges that have been parsed so they can be placed in teh graph
    private LinkedBlockingQueue<siteNode> finishedQueue = new LinkedBlockingQueue<siteNode>();
    //Filters URLs
    private urlFilter filter;
    //Keeps track of the threads that are blocking
    private AtomicInteger blockingThreads;
    //Site Graph
    private Graph graph;
    //All pairs shortest path graph lib object
    private APSP apsp;
    //Tarjans graph lib object
    private TarjanStronglyConnectedComponents tscc;
    //Tool kit for other graph stats
    private Toolkit toolkit;

    //Constructor fpr web cralwer
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

            //Seed URLs
            System.out.println("Webcrawler Seeded with ");

            URL rootURL = new URL(seedUrl);
            System.out.print(rootURL.toString());
            siteNode rootNode = new siteNode(rootURL, null, 0);
            //Add to queue
                frontierQueue.put(rootNode);


            //Start Thread manager
            ExecutorService executor = Executors.newCachedThreadPool();
            //Start Thread
            for (int i = 0; i < numThreads; i++) {
                Runnable worker = new workerThread(frontierQueue, finishedQueue, graphSizeCounter, maxGraphSize, filter, blockingThreads);
                executor.execute(worker);
            }


            int count = 0;

            while (true) {

                try {
                    //Poll queue for value, if no value continue with null
                    siteNode node = finishedQueue.poll(1, TimeUnit.SECONDS);

                    //If not null insert the pulled value
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
            //Calculate and print the graph stats
            printGraphStats();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        }


    }

    private void insertNode(siteNode node) {

        //Get the out going edges list
        LinkedList<URL> edges = node.getOutGoingEdges();

        //Loop through adding them as directed edge to the graph
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

        //Get the degree distribution
        int[] distribution = Toolkit.degreeDistribution(graph);

        System.out.println("Edge Out-Degree Distribution");
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
            numComponents.add((Integer) n.getAttribute("scc"));
        System.out.println(numComponents.size());


        //Computer APSP using FW
        System.out.println("Computing Average Path Length(This can take a while");
        apsp.compute();
        float totalDistance = 0;
        float totalPaths = 0;
        //Calculate Average Shortest Path
        for (Node n : graph.getEachNode()) {
            APSPInfo info = n.getAttribute(APSPInfo.ATTRIBUTE_NAME);
            for (Map.Entry<String, APSP.TargetPath> value : info.targets.entrySet()) {
                APSP.TargetPath path = value.getValue();
                totalDistance += path.distance;
                totalPaths++;
            }
        }

        float average = totalDistance / totalPaths;
        System.out.println("Average Shortest Path Length: " + average);
        graph.display();


    }


}
