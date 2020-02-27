/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The base source code was modified for my submission of the individual assignment
 * for the module 'Information Retrieval and Web Search (CS7IS3)' in Trinity College Dublin"
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.PrintWriter;


import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;

/** Simple command-line based search demo. */
public class Searcher {

    private Searcher() {
    }

    /**
     * Simple command-line based search demo.
     */
    public static void main(String[] args) throws Exception {

        String index = "index";
        String queryString = "";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        String results_path = "results.txt";
        PrintWriter writer = new PrintWriter(results_path, "UTF-8");

        //BM25
        searcher.setSimilarity(new BM25Similarity());

        //---------------- Read in and parse queries ----------------

        String queriesPath = "cran/cran.qry";
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "author", "bibliography", "words"}, analyzer);

        String currentLine = bufferedReader.readLine();

        System.out.println("Reading in queries and creating search results.");

        String id = "";
        int i=0;

        while (currentLine != null) {
            i++;
            if (currentLine.startsWith(".I")) {
                id = Integer.toString(i);
                currentLine = bufferedReader.readLine();
            }
            if (currentLine.startsWith(".W")) {
                currentLine = bufferedReader.readLine();
                while (currentLine != null && !currentLine.startsWith(".I")) {
                    queryString += currentLine + " ";
                    currentLine = bufferedReader.readLine();
                }
            }
            queryString = queryString.trim();
            Query query = parser.parse(QueryParser.escape(queryString));
            queryString = "";
            performSearch(searcher, writer, Integer.parseInt(id), query);
        }

        writer.close();
        reader.close();
    }


    // Performs search and writes results to the writer
    public static void performSearch(IndexSearcher searcher, PrintWriter writer, Integer queryNumber, Query query) throws IOException {
        TopDocs results = searcher.search(query, 1400);
        ScoreDoc[] hits = results.scoreDocs;

        // Write the results for each hit
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            /*
             * Write the results in the format expected by trec_eval:
             * | Query Number | 0 | Document ID | Rank | Score | "EXP" |
             * (https://stackoverflow.com/questions/4275825/how-to-evaluate-a-search-retrieval-engine-using-trec-eval)
             */
            writer.println(queryNumber + " 0 " + doc.get("id") + " " + i + " " + hits[i].score + " EXP");
            System.out.println(queryNumber + " 0 " + doc.get("id") + " " + i + " " + hits[i].score + " EXP");
        }
    }
}
