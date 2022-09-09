import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import com.opencsv.CSVReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import java.util.HashMap;

public class example2 {
    public static void main(String[] args) throws IOException, ParseException, CsvException {

        long start;
        long end;

        // Maximum character limit in cells
        int cellCharMax = 10;
        int nRows = 5;
        int hitsPerPage = 10;

        // Read csv file
        start = System.nanoTime();
        System.out.println("\n> Reading csv");
        List<String[]> lines = readCsv("./LearnLucene/src/assets/pitt/pitt-dataset.csv").subList(0, 15000);
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        // Print the data set
        start = System.nanoTime();
        System.out.printf("\n> Printing %s lines\n", nRows);
        printRows(lines, cellCharMax, nRows);
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        // Create analyzer and index
        start = System.nanoTime();
        System.out.println("\n> Creating analyzer and index");
        Analyzer analyzer = new StandardAnalyzer();
        Directory index = new ByteBuffersDirectory();
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        // Create index writer config
        start = System.nanoTime();
        System.out.println("\n> Configuring IndexWriter");
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        // Create index writer, using
        //    given index and configuration based on the analyzer.
        start = System.nanoTime();
        System.out.println("\n> Creating IndexWriter");
        IndexWriter writer = new IndexWriter(index, config);
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        String[] headerFocus = {"report_num", "type_patient", "type_report", "chief_comp", "code", "year"};

        // Populate the corpus
        start = System.nanoTime();
        System.out.println("\n> Populating lucene");
        populateCorpus(lines, headerFocus, 0, -1, writer);
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        // Close the writer once we are done.
        writer.close();

        // Create query string
        start = System.nanoTime();
        System.out.println("\n> Creating queryStr");
        String queryStr = args.length > 0 ? args[0] : "consult";
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        // Create actual query
        start = System.nanoTime();
        System.out.println("\n> Creating query");
        Query query = new QueryParser("type_patient", analyzer).parse(queryStr);
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );

        start = System.nanoTime();
        searchRow(query, index, hitsPerPage);
        end = System.nanoTime();
        System.out.printf("Time taken: %s s \n", String.valueOf((end - start)/1e+9 ) );
    }

    public static void searchRow(
            Query query,
            Directory index,
            int hitsPerPage
            ) throws IOException {

        // Create search
        IndexReader reader = DirectoryReader.open(index); // Create IndexReader
        IndexSearcher searcher = new IndexSearcher(reader); // Create IndexSearcher
        TopDocs topDocs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = topDocs.scoreDocs;

        // Print out results
        for (int i = 0; i < hits.length; i++) {
            int id = hits[i].doc;
            Document doc = searcher.doc(id);

            if (i+1 < 10) {
                System.out.printf("(%s).  ", i + 1);
            }
            else {
                System.out.printf("(%s). ", i + 1);
            }

            System.out.printf(
                    "%s \t \"%s\" %s %n",
                    doc.get("chief_comp"),
                    doc.get("code"),
                    doc.get("type_patient")
            );
        }

    }

    public static void printRows(List<String[]> data, int cellCharMax, int nRows) {
        // Iterate through data set
        for (int i = 0; i < nRows + 1; i++) {
            // Iterate through single row
            System.out.printf("(%s). ", i);
            for (int j = 0; j < data.get(i).length; j++) {
                if (data.get(i)[j].length() < 1) {
                    System.out.print("%s \t | \t".formatted(
                            new String(new char[cellCharMax])).replace('\0', ' '));
                }
                else if (data.get(i)[j].length() < cellCharMax) {
                    System.out.printf(
                            "%s%s \t | \t", data.get(i)[j],
                            new String(new char[cellCharMax - data.get(i)[j].length()]).replace('\0', ' '));
                }
                else {
                    System.out.printf("%s \t | \t", data.get(i)[j].substring(0, cellCharMax));
                }
            }
            System.out.print("\n");
        }
        System.out.println("\n");
    }

    public static List<String[]> readCsv(String path) throws IOException, CsvException {
        List<String[]> data;

        try ( // automatic resource management
                FileReader fileReader = new FileReader(path);
                CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(0).build()
        ) {
            data = csvReader.readAll();
        }
        return data;
    }

    /* Uses IndexWriter w to populate the corpus so that Lucene can access it */
    public static void populateCorpus(List<String[]> data, int headerRow, int nDocs, IndexWriter w)
            throws IOException {

        // In case we populate more than needed
        if (data.size() < nDocs) {
            nDocs = data.size();
        }
        else if (nDocs == -1) { // In case we want ALL
            nDocs = data.size();
        }

        // Iterate through every row
        for (int i = headerRow + 1; i < nDocs; i++) {

            Document doc = new Document();

            // Iterate through header row, populate document
            for (int j = 0; j < data.get(headerRow).length; j++) {
                doc.add(new TextField(data.get(headerRow)[j], data.get(i)[j], Field.Store.YES));
            }

            w.addDocument(doc);
        }
    }

    /* Uses IndexWriter w to populate the corpus so that Lucene can access it.
    * Additionally, this function specifies a headerFocus, which only uses
    * certain specified columns to populate the data set.
    * */
    public static void populateCorpus(
            List<String[]> data,
            String[] headerFocus,
            int headerRow,
            int nDocs,
            IndexWriter w
    ) throws IOException {

        // In case we populate more than needed
        if (data.size() < nDocs) {
            nDocs = data.size();
        }
        else if (nDocs == -1) { // In case we want ALL
            nDocs = data.size();
        }

        // Create hashmap to determine if cell is in focused column
        // Will also allow the population to be faster due to column index as
        // the value in the hash map
        HashMap<String, Integer> headerHash = new HashMap<String, Integer>();
        // Populate the hash map
        for (int i = 0; i < headerFocus.length; i++) {

            String focusColumn = headerFocus[i];

            // Get the index of the column
            for (int j = 0; j < data.get(headerRow).length; j++) {

                // List of ALL columns in the data
                String checkColumn = data.get(headerRow)[j];

                // If they match, we want "columnName" : (int)columnIndex
                if (focusColumn.equals(checkColumn)) {
                    headerHash.put(focusColumn, j);
                }

            }
        } // Now that we have the hash map, we can iterate through the cells without
        // having to move through every single column and cell.

        // Iterate through every row
        for (int i = headerRow + 1; i < nDocs; i++) {

            Document doc = new Document();

            // Iterate through header focus, populate lucene
            for (int j = 0; j < headerFocus.length; j++) {
                int xIndex = headerHash.get(headerFocus[j]);
                doc.add(new TextField(data.get(headerRow)[ xIndex ], data.get(i)[ xIndex ], Field.Store.YES));
            }

            w.addDocument(doc);
        }
    }
}
