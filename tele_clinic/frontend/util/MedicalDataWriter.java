package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Writes student medical registration data to a CSV file.
 * Demonstrates file I/O and exception handling (try-with-resources).
 */
public class MedicalDataWriter {

    private static final String FILE_NAME = "medical_registrations.csv";
    // FIX: Updated HEADER to include GuardianName and GuardianContact (12 columns total)
    private static final String CSV_HEADER = 
        "FullName,BITS_ID,Gender,BITS_Email,MobileNo,TelegramNo,BloodType,Allergies,ChronicIllnesses,InsuranceType,GuardianName,GuardianContact";

    /**
     * Appends a new user's medical and personal details to a CSV file.
     * @param data An array of strings containing the collected form data.
     * @return true if the write was successful, false otherwise.
     * Demonstrates Exception Handling.
     */
    public static boolean writeDataToCsv(String[] data) {
        File csvFile = new File(FILE_NAME);
        boolean isNewFile = !csvFile.exists();

        try (FileWriter fw = new FileWriter(csvFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            // Write header only if the file is new or old header is missing
            if (isNewFile || !hasCorrectHeader(csvFile)) {
                pw.println(CSV_HEADER);
            }

            // Prepare the data line
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < data.length; i++) {
                // Wrap in quotes and escape internal quotes
                sb.append("\"").append(data[i].replace("\"", "\"\"")).append("\"");
                // Add comma unless it's the last item
                if (i < data.length - 1) {
                    sb.append(",");
                }
            }
            
            pw.println(sb.toString());
            System.out.println("DEBUG: Successfully wrote data to CSV: " + data[0] + ", " + data[1]);
            return true;

        } catch (IOException e) {
            System.err.println("Error writing data to CSV file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to check if the existing file has the correct header.
     * Simplistic check only used for new file creation logic.
     */
    private static boolean hasCorrectHeader(File csvFile) {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFile))) {
            String headerLine = br.readLine();
            // Perform a robust check that involves stripping quotes and spaces if necessary,
            // but for simplicity, we check if the line contains the main fields expected.
            return headerLine != null && headerLine.contains("GuardianName") && headerLine.contains("GuardianContact");
        } catch (IOException e) {
            // Assume header check failed if file cannot be read
            return false; 
        }
    }
}