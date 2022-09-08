import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class example2 {
    public static void main(String[] args) throws IOException, ParseException, CsvException {

        // Maximum character limit in cells
        int cellCharMax = 30;
        int nRows = 5;

        // Read csv file
        List<String[]> lines = readCsv("./src/assets/harry_potter/titles.csv");

        // Print the data set
        printRows(lines, cellCharMax, nRows);
    }

    public static void printRows(List<String[]> data, int cellCharMax, int nRows) {
        // Iterate through data set
        for (int i = 0; i < nRows + 1; i++) {
            // Iterate through single row
            System.out.print("(%s). \t".formatted(i));
            for (int j = 0; j < data.get(i).length; j++) {
                if (data.get(i)[j].length() < 1) {
                    System.out.print("%s \t | \t".formatted(
                            new String(new char[cellCharMax])).replace('\0', ' '));
                }
                else if (data.get(i)[j].length() < cellCharMax) {
                    System.out.print("%s%s \t | \t".formatted(
                            data.get(i)[j],
                            new String(new char[cellCharMax - data.get(i)[j].length()]).replace('\0', ' ')));
                }
                else {
                    System.out.print("%s \t | \t".formatted(data.get(i)[j].substring(0, cellCharMax)));
                }
            }
            System.out.print("\n");
        }
    }

    public static List<String[]> readCsv(String path) throws IOException, CsvException {
        // Read the file
        FileReader fileReader = new FileReader(path);
        // Read CSV based on file
        CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(0).build();
        // Return List of string arrays representing each cell
        return csvReader.readAll();
    }

    public static void populateCorpus(List<String[]> data, int headerRow, int nDocs) {
        return;
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
