import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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

public class example2 {
    public static void main(String[] args) throws IOException, ParseException, CsvException {

        // Maximum character limit in cells
        int cellCharMax = 10;
        int nRows = 5;
        int hitsPerPage = 10;

        // Read csv file
        List<String[]> lines = readCsv("./LearnLucene/src/assets/pitt/pitt-dataset.csv").subList(0, 3000);

        // Print the data set
        printRows(lines, cellCharMax, nRows);

        // Create analyzer and index
        Analyzer analyzer = new StandardAnalyzer();
        Directory index = new ByteBuffersDirectory();

        // Create index writer config
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // Create index writer, using
        //    given index and configuration based on the analyzer.
        IndexWriter writer = new IndexWriter(index, config);

        // Populate the corpus
        populateCorpus(lines, 0, -1, writer);

        // Close the writer once we are done.
        writer.close();

        // Create query string
        String queryStr = args.length > 0 ? args[0] : "consult";

        // Create actual query
        Query query = new QueryParser("type_patient", analyzer).parse(queryStr);

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
                    doc.get("body")
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

    // Uses IndexWriter w to populate the corpus so that Lucene can access it
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
}
