# Lucene-Search_IRWS-Individual
A Lucene search application built using Java as part of coursework for the 'Information Retrieval and Web Search (CS7IS3)' module at Trinity College Dublin.

Step 1: Change directory into the 'Lucene-Search_IRWS-Individual' folder.
        `cd Lucene-Search_IRWS-Individual/`

Step 2: Do a maven clean to start afresh.
        `mvn clean`

Step 3: Do a maven install to download dependencies and make the program executable.
        `mvn install`

Step 4: The next command will allow you to run the app. It will first index the documents and then search it.
        `mvn exec:java -Dexec.mainClass="App"`

Step 5: The next command will run the 'trec_eval' tool to evaluate the results obtained.
        `./trec_eval-9.0.7/trec_eval QRelsCorrectedforTRECeval results.txt`
