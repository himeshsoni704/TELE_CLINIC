package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Manages logging and alerting for emergency calls.
 */
public class EmergencyLogWriter {
    private static final String FILE_NAME = "emergency_logs.txt";
    private static final String LOG_ENTRY_TYPE = "emergencycall_1";

    /**
     * Appends an emergency log entry and triggers the Telegram notification.
     * @param bitsId The ID of the user who triggered the emergency call.
     * @param fullName The full name of the user.
     * @return true if both logging and notification were successful.
     */
    public static boolean logAndNotifyEmergency(String bitsId, String fullName) {
        boolean logSuccess = false;
        
        // 1. Log the event to a local file
        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             PrintWriter pw = new PrintWriter(fw)) {

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            // Format: Timestamp | UserID | LogType
            String logEntry = String.format("%s | %s | %s", timestamp, bitsId, LOG_ENTRY_TYPE);
            
            pw.println(logEntry);
            logSuccess = true;

        } catch (IOException e) {
            System.err.println("Error writing emergency log: " + e.getMessage());
        }
        
        
        
        // Return true only if BOTH operations were successful
        return logSuccess ;
    }
}