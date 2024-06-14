import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class OutputWriter {
    public static void writeOutput(List<String> results) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            for (String result : results) {
                writer.write(result);
                writer.newLine();
            }
        }
    }

}
