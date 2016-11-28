package com.webcrawl.app;

import org.apache.commons.cli.*;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Hello world!
 *
 */


public class App 
{

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        //make options object and set values to handle command line parsing
        Options options = new Options();

        //get cmd
        options.addOption("s", true, "seed Url");
        options.addOption("w", true, "number of workerThread threads");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            // validate that block-size has been set
            if (line.hasOption("w")) {
                // print the value of block-size
                int numThreads = Integer.parseInt(line.getOptionValue("w"));

                try {
                    URI seedURL = new URI(line.getOptionValue("s"));
                } catch (URISyntaxException e) {
                    System.out.println(e);
                    System.exit(1);
                }

            }
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            System.exit(1);
        }

    }
}
