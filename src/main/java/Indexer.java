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
 * The base source code from the Apache website was modified as part of my submission for
 * the module 'Information Retrieval and Web Search (CS7IS3)' in Trinity College Dublin
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    private Indexer() {}

    /** Index all text files under a directory. */
    public static void main(String[] args) {
        String indexPath = "index";
        String docsPath = "cran/cran.all.1400";

        final Path docDir = Paths.get(docsPath);

        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));

            //Analyzer analyzer = new SimpleAnalyzer();
            //Analyzer analyzer = new WhitespaceAnalyzer();
            //Analyzer analyzer = new StandardAnalyzer();
            Analyzer analyzer = new EnglishAnalyzer();

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            //BM25 Similarity
            iwc.setSimilarity(new BM25Similarity());

            //Classic Similarity
            //iwc.setSimilarity(new ClassicSimilarity());

            //LMDirichletSimilarity
            //iwc.setSimilarity(new LMDirichletSimilarity());

            //Trying a multi similarity model
            //iwc.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));

            //Trying another multi similarity model
            //iwc.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new LMDirichletSimilarity()}));

            //Trying another multi similarity model
            //iwc.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity()}));

            iwc.setOpenMode(OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDoc(writer, docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }



    /** Indexes a single document */
    static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            Boolean first = true;
            System.out.println("Indexing documents.");

            String currentLine = bufferedReader.readLine();
            String fullText = "";
            int i=1;

            while(currentLine != null){
                Document doc = new Document();
                if(currentLine.startsWith(".I")){
                    doc.add(new StringField("id", currentLine.substring(3), Field.Store.YES));
                    currentLine = bufferedReader.readLine();
                }
                if (currentLine.startsWith(".T")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".A")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("title", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".A")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".B")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("author", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".B")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".W")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("bibliography", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".W")){
                    currentLine = bufferedReader.readLine();
                    while(currentLine != null && !currentLine.startsWith(".I")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("words", fullText, Field.Store.YES));
                    fullText = "";
                }
                writer.addDocument(doc);
            }
        }
    }
}

