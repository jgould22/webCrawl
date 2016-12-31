#READ ME

This is mainly a program for me to play around and learn some of Java's concurrency API, as such it is **NOT very robust or efficient**

* Once it has reached it max graph size it calculates a few statistics
    * -Average path length
    * -Graph Diameter
    * -Out-degree distribution
    * -# of Strongly connected components
    * -Displays basic graph image

* This program takes 3 parameters, all are required
    * -w, the number of worker threads
    * -s the seed URL
    * -gs the MAX graph size (number of internal nodes)

eg.
    *-w 10 -s http://www.brantford.ca -gs 50

This project uses apache maven to get dependencies
in the folder with the pom.xml run "mvn package"

*Dependancies
    *-Graphstream
        *-algo
        *-Core
    *-jsoup

*Limitations
    *-Tarjan's(SCC Algorithm) and Floyd–Warshall (APSP Algorithm) are used and are not very efficient, large graph sizes can take a great deal of time
    *-No Robots.txt handing, this is not an ethical crawler 
    *-No strong URL resolving, if there is an error, the URL is ignored and not crawled
    *-Graph is stored in RAM, large graph sizes are not recommended
