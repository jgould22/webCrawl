READ ME
This program takes 3 parameters, all are required
-w, the number of worker threads
-s the seed URL
-gs the graph size

eg
-w 10 -s http://www.brantford.ca -gs 200

This project uses apache maven to get dependancies 
in the folder with the pom.xml run "mvn package"

Known Issues
Sometimes a URL fails to resolve as the resolve code is not robust, this may crash a thread

This is not an ethical web crawler, I had to remove the robots txt handing codes due to some issues getting it to work