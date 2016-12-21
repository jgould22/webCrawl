package com.webcrawl.app;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by jordan on 16/12/16.
 * This is a simple singleton to handle URL filtering
 * This class will filter already visited URLs as well as robot.txt files
 */

public class urlFilter {

    private static urlFilter instance = null;
    //HashSet of hosted visted
    private HashSet<String> hostsVistied;
    //nodesVisite Hashset of visited nodes
    private HashSet<URL> nodesVisited;
    private Map<String, BaseRobotRules> robotsTxtRules;

    protected urlFilter() {
        // Exists only to defeat instantiation.
    }

    public static urlFilter getInstance() {
        if (instance == null) {
            instance = new urlFilter();
            instance.hostsVistied = new HashSet<String>();
            instance.nodesVisited = new HashSet<URL>();
            instance.robotsTxtRules = new HashMap<String, BaseRobotRules>();
        }
        return instance;
    }


    //Test harness for urlFiler
    public static void main(String[] args) {

        urlFilter filter = urlFilter.getInstance();

        try {
            URL testURL = new URL("http://www.uwo.ca");
            URL testURL2 = new URL("http://www.uwo.ca/uwocom/cascade/");
            boolean testFalse = filter.urlAlreadyVisited(testURL);
            filter.addURL(testURL);

            boolean testTrue = filter.urlAlreadyVisited(testURL);
            boolean testTrue2 = filter.urlAlreadyVisited(testURL2);

            //Prints false since its a new url
            System.out.println(testFalse);

            //Prints true since it is visited
            System.out.println(testTrue);

            //Prints true since it is in uwo robots.txt
            System.out.println(testTrue2);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    //Returns true if url has already been visited
    public synchronized boolean urlAlreadyVisited(URL url) {

        String hostId = url.getProtocol() + "://" + url.getHost()
                + (url.getPort() > -1 ? ":" + url.getPort() : "");

        //get the rule set for this host
        BaseRobotRules rules = robotsTxtRules.get(hostId);
        //if the rules dont exist
        if (rules == null) {

            try {

                URL robotsurl = new URL(hostId + "/robots.txt");
                BufferedReader bis;
                String s;
                String page = "";


	        /* URL that the web crawler will download. You might change this
             URL to download other pages                                   */
                bis = new BufferedReader(new InputStreamReader(robotsurl.openStream()));

                while ((s = bis.readLine()) != null) {
                    page += s;
                }
                bis.close();

                SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
                byte[] bytes = page.getBytes();
                rules = robotParser.parseContent(hostId, bytes,
                        "text/plain", "*");
            } catch (MalformedURLException mue) {
                mue.printStackTrace();
            } catch (IOException ioe) {
                rules = new SimpleRobotRules(SimpleRobotRules.RobotRulesMode.ALLOW_ALL);
            }


            robotsTxtRules.put(hostId, rules);

        }

        boolean urlAllowed = rules.isAllowed(url.toString());

        return urlAllowed;
/*
        //check to see if host has been visited
        if (!hostsVistied.contains(url.getHost())) {

            //download and parse robots.txt
            downloadParseRobots(url);

            //check if the nodes are visted/filtered
            return nodesVisited.contains(url);

        } else { //host has already been visted so already taken robots.text into account

            return nodesVisited.contains(url);

        }
*/

    }

    public synchronized void addURL(URL url) {

        if (!hostsVistied.contains(url.getHost())) {
            //download and parse robots.txt
            downloadParseRobots(url);

            //check if the nodes are visted/filtered
            nodesVisited.add(url);

        } else { //host has already been visted so already taken robots.text into account

            nodesVisited.add(url);

        }


    }

    private void downloadParseRobots(URL host) {

        try {
            //Create new URL for the robots txt
            URI robotsURL = new URI(
                    host.getProtocol(),
                    host.getHost(),
                    "/robots.txt",
                    null);


            //Open input stream reader
            try (BufferedReader in = new BufferedReader(new InputStreamReader(robotsURL.toURL().openStream()))) {

                String line = null;
                boolean foundAgrentLine = false;
                //Loop through looking for the every user agent line, after that add all disallowed to nodes visited
                URI newURL;
                while ((line = in.readLine()) != null) {
                    if (line.contains("User-agent: *")) {
                        foundAgrentLine = true;
                    } else if (foundAgrentLine == true && (line.equals("") || line.contains("#"))) {
                        break;
                    } else if (foundAgrentLine == true && !line.equals("")) {

                        //get path and make url
                        newURL = new URI(
                                host.getProtocol(),
                                host.getHost(),
                                line.substring(line.indexOf(' ') + 1),
                                null);
                        //Normalize URL
                        newURL = newURL.normalize();

                        nodesVisited.add(newURL.toURL());
                    }

                }
                //host has been visited for first time so add it to the list, we dont need to get its robots twice
                hostsVistied.add(host.getHost());

            } catch (IOException e) {

                e.printStackTrace();

            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
