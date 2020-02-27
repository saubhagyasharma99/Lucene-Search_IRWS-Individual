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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
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
            System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(new BM25Similarity());

            iwc.setOpenMode(OpenMode.CREATE);

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDoc(writer, docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            //writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     *
     * NOTE: This method indexes one document per input file.  This is slow.  For good
     * throughput, put multiple documents into your input file(s).  An example of this is
     * in the benchmark module, which can create "line doc" files, one document per line,
     * using the
     * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     *
     * //@param writer Writer to the index where the given file/dir info will be stored
     * //@param path The file to index, or the directory to recurse into to find files to index
     * @throws IOException If there is a low-level I/O error
     */

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

