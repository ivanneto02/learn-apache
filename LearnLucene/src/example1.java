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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;

public class example1 {
    public static void main(String[] args) throws IOException, ParseException {

        // Create analyzer and index
        Analyzer analyzer = new StandardAnalyzer();
        Directory index = new ByteBuffersDirectory();

        // Create index writer config
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // Create index writer, using
        //    given index and configuration based on the analyzer.
        IndexWriter writer = new IndexWriter(index, config);

        addDoc(writer, "hello_world", "I am writing this to say hello to the world");
        addDoc(writer, "goodbye_world", "I am writing this to say goodbye to the entire world.");
        addDoc(writer, "phone", "The phone is an amazing creation that has been made possible by unbelievable human strides.");

        // Close the writer once we are done.
        writer.close();

        // Create query string
        String queryStr = args.length > 0 ? args[0] : "phone";
        // Create actual query
        Query query = new QueryParser("text", analyzer).parse(queryStr);

        // Create search
        int hitsPerPage = 3;
        IndexReader reader = DirectoryReader.open(index); // Create IndexReader
        IndexSearcher searcher = new IndexSearcher(reader); // Create IndexSearcher
        TopDocs topDocs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = topDocs.scoreDocs;

        for (int i = 0; i < hits.length; i++) {
            int id = hits[i].doc;
            Document doc = searcher.doc(id);

            System.out.println("(%s). %s \t \"%s\"".formatted(
                    i + 1,
                    doc.get("title"),
                    doc.get("text")
            ));
        }
    }

    // Keep in mind we use TextField for fields which we want tokenized
    // and StringField for fields which we DO NOT want tokenized
    public static void addDoc(IndexWriter w, String title, String text) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));
        w.addDocument(doc);
    }
}
