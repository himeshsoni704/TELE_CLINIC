package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to write prescription details to a dedicated CSV file.
 */
public class PrescriptionWriter {

    private static final String FILE_NAME = "prescriptions.txt";
    private static final String CSV_HEADER = "StudentID,Timestamp,StaffID,StaffName,Medication,Notes";

    /**
     * Appends a new prescription entry to the CSV file.
     * @param data An array of strings: [StudentID, Timestamp, StaffID, StaffName, Medication, Notes].
     * @return true if the write was successful, false otherwise.
     */
    public static boolean writeDataToCsv(String[] data) {
        File csvFile = new File(FILE_NAME);
        boolean isNewFile = !csvFile.exists();

        try (FileWriter fw = new FileWriter(csvFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            if (isNewFile || fileIsEmpty(csvFile)) {
                pw.println(CSV_HEADER);
            }

            // Simple CSV encoding (wraps fields in quotes and joins)
            String csvLine = Stream.of(data)
                .map(s -> "\"" + s.replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining(","));
            
            pw.println(csvLine);
            return true;

        } catch (IOException e) {
            System.err.println("Error writing prescription data to CSV file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper to check if file is empty
    private static boolean fileIsEmpty(File file) {
        return file.length() == 0;
    }
}