package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Manages user credentials (ID, Password, Role) stored in a CSV file.
 * Demonstrates basic file I/O and exception handling (try-with-resources).
 */
public class CredentialManager {
    private static final String FILE_PATH = "credentials.csv";
    private static final String DELIMITER = ",";
    private static final String CSV_HEADER = "Username,Password,Role";

    /**
     * Ensures the credential file exists and has a header. Creates mock data if new.
     */
    private void ensureFileExists() {
        if (!Files.exists(Paths.get(FILE_PATH))) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
                pw.println(CSV_HEADER);
            } catch (IOException e) {
                System.err.println("Error initializing credential file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifies if the provided username and password exist in the CSV file.
     * @return An Optional containing the Role (e.g., "Student", "Nurse", or "Admin") if found.
     */
    public Optional<String> verifyCredentials(String username, String password) {
        ensureFileExists();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            reader.readLine(); // Skip the header
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 3) {
                    // Note: Removed redundant replace("\"", "") calls as input data doesn't have quotes
                    String storedUsername = parts[0].trim();
                    String storedPassword = parts[1].trim();
                    String storedRole = parts[2].trim();
                    
                    if (storedUsername.equals(username) && storedPassword.equals(password)) {
                        return Optional.of(storedRole); // Match found
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // No match found
    }
    
    /**
     * Adds a new user credential to the file.
     */
     public boolean addCredential(String username, String password, String role) {
        ensureFileExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            // Simple check to prevent duplicates
            if (verifyCredentials(username, "nonexistent").isPresent()) {
                return false; // User already exists
            }
            
            String line = username + DELIMITER + password + DELIMITER + role;
            writer.write(line);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}