import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class InputParser {
    private String xmlFile;
    private final List<String> queries;

    public InputParser(String filename) throws Exception {
        queries = new ArrayList<>();
        parseInputFile(filename);
    }

    private void parseInputFile(String filename) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            xmlFile = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                queries.add(line);
            }
        }
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public List<String> getQueries() {
        return queries;
    }
}
