# Lucene-Search_IRWS-Individual
A Lucene search application built using Java as part of coursework for the 'Information Retrieval and Web Search (CS7IS3)' module at Trinity College Dublin.

## To build and run the program follow the steps below:

Step 1: Do a maven clean to start the project afresh.  
        `mvn clean`

Step 2: Do a maven install to download dependencies and make the program executable.  
        `mvn install`

Step 3: The next command will allow you to run the app. It will first index the documents and then search it.  
        `mvn exec:java -Dexec.mainClass="App"`  
        The index that was built will be available in the 'index' folder.  
        The results of the search operation will be written to the "results.txt" file which is in the root of the project folder.

Step 4: The next command will run the 'trec_eval' tool to evaluate the results obtained.  
        `./trec_eval-9.0.7/trec_eval QRelsCorrectedforTRECeval results.txt`

## The 'Lucene-Search_IRWS-Individual' folder contains the following documents:
  - A copy of my Java source code
  - A copy of trec_eval (It is already built. Does not need the "make" command.)
  - A copy of Cranfield and the corrected 'QRelsCorrectedforTRECeval' file that was provided
  - This README
