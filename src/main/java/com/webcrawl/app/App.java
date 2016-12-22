package com.webcrawl.app;

import org.apache.commons.cli.*;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Hello world!
 */


public class App {

    public static void main(String[] args) {
        int numThreads = 0;
        Long maxGraphSize;
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        //make options object and set values to handle command line parsing
        Options options = new Options();

        //get cmd
        options.addOption("s", true, "seed Url");
        options.addOption("w", true, "number of workerThread threads");
        options.addOption("gs", true, "number of workerThread threads");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            // validate that block-size has been set
            if (line.hasOption("w")) {
                // print the value of block-size
                numThreads = Integer.parseInt(line.getOptionValue("w"));

                try {
                    URI seedURL = new URI(line.getOptionValue("s"));
                } catch (URISyntaxException e) {
                    System.out.println(e);
                    System.exit(1);
                }

            }

            //Get max graph size option
            maxGraphSize = Long.parseLong(line.getOptionValue("gs"));

            //start webcrawler
            webCrawler webCrawler = new webCrawler(numThreads, maxGraphSize);
            webCrawler.startCrawl(line.getOptionValue("s"));

        } catch (ParseException exp) {
            exp.printStackTrace();
            System.exit(1);
        }

    }
}
