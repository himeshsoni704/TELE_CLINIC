package bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Notifier - Maps Student IDs to Guardian Telegram IDs and sends alerts
 * FIXED: Corrected CSV column indices to match your actual CSV structure
 */
public class Notifier {

	private static final String MESSAGES_FILE = "messages.txt";
	private static final String EMERGENCY_LOG_FILE = "..\\..\\frontend\\emergency_logs.txt";
	private static final String STUDENTS_FILE = "..\\..\\frontend\\medical_registrations.csv";
	private static final Gson gson = new Gson();

	private final Map<String, String> studentToGuardian = new HashMap<>();
	private final Map<String, String> studentIdToName = new HashMap<>();
	private final AutoReplyBot bot;

	// ANSI color codes
	private static final String GREEN = "\u001B[32m";
	private static final String YELLOW = "\u001B[33m";
	private static final String RED = "\u001B[31m";
	private static final String RESET = "\u001B[0m";

	public Notifier(AutoReplyBot bot) throws Exception {
		this.bot = bot;
		bot.registerBot();
		loadDatabase();
	}

	/**
	 * FIXED: Updated to match your CSV structure
	 * CSV Header: FullName(0), BITS_ID(1), Gender(2), BITS_Email(3), MobileNo(4), 
	 *            TelegramNo(5), BloodType(6), Allergies(7), ChronicIllnesses(8), 
	 *            InsuranceType(9), GuardianName(10), GuardianContact(11)
	 */
	private void loadDatabase() {
		File file = new File(STUDENTS_FILE);
		if (!file.exists()) {
			System.out.println(RED + "[ERROR] Database file not found at: " + file.getAbsolutePath() + RESET);
			return;
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int rowNum = 0;
			while ((line = br.readLine()) != null) {
				if (rowNum++ == 0) continue; // Skip header

				// Remove quotes and split by comma
				line = line.replaceAll("\"", "");
				String[] parts = line.split(",");

				// FIXED: Corrected indices for your CSV structure
				// parts[0] = FullName
				// parts[1] = BITS_ID
				// parts[5] = TelegramNo (Student's Telegram)
				// parts[10] = GuardianName
				// parts[11] = GuardianContact (Guardian's Telegram)
				
				if (parts.length >= 12) {
					String studentName = parts[0].trim();
					String bitsId = parts[1].trim();              // Key: BITS ID
					String studentTelegramId = parts[5].trim();  // FIXED: Index 5 (was 6)
					String guardianName = parts[10].trim();
					String guardianTelegramId = parts[11].trim(); // Value: Guardian's Telegram ID

					if (!guardianTelegramId.isEmpty() && !bitsId.isEmpty()) {
						// Map BITS ID -> Guardian (For emergency_logs.txt which sends BITS_ID)
						studentToGuardian.put(bitsId, guardianTelegramId);
						studentIdToName.put(bitsId, studentName);
						
						System.out.println(GREEN + "[LOADED] " + studentName + " (" + bitsId + 
							") -> Guardian: " + guardianName + " (" + guardianTelegramId + ")" + RESET);
					}
				}
			}
			System.out.println(GREEN + "[INFO] Database loaded successfully!" + RESET);
			System.out.println(GREEN + "[INFO] Student -> Guardian Mapping: " + studentToGuardian.size() + " records" + RESET);

		} catch (IOException e) {
			System.out.println(RED + "[ERROR] Failed to load database: " + e.getMessage() + RESET);
			e.printStackTrace();
		}
	}

	/**
	 * Monitors both messages.txt (for location logs) and emergency_logs.txt (for urgent button clicks)
	 */
	public void watchMessagesFile() throws IOException, InterruptedException {
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Map<WatchKey, Path> keys = new HashMap<>();

		// 1. Setup Watcher for MESSAGES_FILE
		File msgFile = new File(MESSAGES_FILE);
		Path msgDir = Paths.get(MESSAGES_FILE).getParent();
		if (msgDir != null) {
			WatchKey key = msgDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			keys.put(key, msgDir);
		}

		// 2. Setup Watcher for EMERGENCY_LOG_FILE
		File emgFile = new File(EMERGENCY_LOG_FILE);
		Path emgDir = Paths.get(EMERGENCY_LOG_FILE).getParent();
		if (emgDir != null && !emgDir.equals(msgDir)) {
			WatchKey key = emgDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			keys.put(key, emgDir);
		}

		System.out.println(GREEN + "[INFO] Watching files for new logs..." + RESET);
		System.out.println("   - " + MESSAGES_FILE);
		System.out.println("   - " + EMERGENCY_LOG_FILE);

		// Track file sizes
		long lastMsgSize = msgFile.exists() ? msgFile.length() : 0;
		long lastEmgSize = emgFile.exists() ? emgFile.length() : 0;

		// Buffers
		StringBuilder msgBuffer = new StringBuilder();
		int openBraces = 0;

		while (true) {
			WatchKey key = watchService.take();
			Path dir = keys.get(key);

			if (dir == null) {
				key.reset();
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				Path changed = (Path) event.context();
				File changedFile = dir.resolve(changed).toFile();

				// === CASE 1: Messages File Changed (Location Log) ===
				if (changedFile.getAbsolutePath().equals(msgFile.getAbsolutePath())) {
					long newSize = msgFile.length();
					if (newSize > lastMsgSize) {
						try (RandomAccessFile raf = new RandomAccessFile(msgFile, "r")) {
							raf.seek(lastMsgSize);
							String line;
							while ((line = raf.readLine()) != null) {
								line = line.trim();
								if (line.isEmpty()) continue;

								// Basic JSON parsing logic
								for (char c : line.toCharArray()) {
									if (c == '{') openBraces++;
									else if (c == '}') openBraces--;
								}
								msgBuffer.append(line);

								if (openBraces == 0 && msgBuffer.length() > 0) {
									processMessageLog(msgBuffer.toString());
									msgBuffer.setLength(0);
								}
							}
						}
						lastMsgSize = newSize;
					}
				}

				// === CASE 2: Emergency File Changed (Button Click Log) ===
				else if (changedFile.getAbsolutePath().equals(emgFile.getAbsolutePath())) {
					long newSize = emgFile.length();
					if (newSize > lastEmgSize) {
						try (RandomAccessFile raf = new RandomAccessFile(emgFile, "r")) {
							raf.seek(lastEmgSize);
							String line;
							while ((line = raf.readLine()) != null) {
								processEmergencyLog(line.trim());
							}
						}
						lastEmgSize = newSize;
					}
				}
			}
			key.reset();
		}
	}

	// --- LOGIC FOR MESSAGES.TXT (JSON Location Data) ---
	private void processMessageLog(String jsonLine) {
		try {
			System.out.println(GREEN + "[INFO] New Location Log: " + jsonLine + RESET);
			JsonObject json = gson.fromJson(jsonLine, JsonObject.class);

			if (json == null || !json.has("chatId")) return;

			String studentId = json.get("chatId").getAsString();
			String note = json.has("text") ? json.get("text").getAsString() : "Location sent";

			sendTelegramAlert(studentId, "Note: " + note);

		} catch (JsonSyntaxException e) {
			System.out.println(RED + "[WARN] Bad JSON in messages.txt" + RESET);
		}
	}

	// --- LOGIC FOR EMERGENCY_LOGS.TXT (Raw ID from Frontend) ---
	private void processEmergencyLog(String logLine) {
		if (logLine.isEmpty()) return;

		System.out.println(RED + "[URGENT] New Emergency Log Entry: " + logLine + RESET);

		String studentId = logLine;

		// Parse "Timestamp | StudentID | Event" if pipe exists
		if (logLine.contains("|")) {
			String[] parts = logLine.split("\\|");
			if (parts.length >= 2) {
				studentId = parts[1].trim(); // Extract the BITS ID
			}
		}

		// Send alert to guardian
		String alertText = "EMERGENCY ALERT RECEIVED! \n" +
						 "Source: Campus Clinic Emergency Button\n" +
						 "Student ID: " + studentId + "\n" +
						 "An emergency alert has been logged by the student.";

		sendTelegramAlert(studentId, alertText);
	}

	// --- SHARED HELPER TO SEND TO TELEGRAM ---
	private void sendTelegramAlert(String studentId, String messageContent) {
		String guardianId = studentToGuardian.get(studentId);
		String studentName = studentIdToName.get(studentId);

		if (guardianId != null && studentName != null) {
			String finalMessage = "Student: " + studentName + " (" + studentId + ")\n\n" + messageContent;

			System.out.println(YELLOW + "[SENDING] To Guardian (" + guardianId + "): " + finalMessage + RESET);
			bot.sendText(guardianId, finalMessage);

		} else {
			System.out.println(RED + "[ERROR] Unknown Student ID: " + studentId + 
				" - Cannot find guardian. Available IDs: " + studentToGuardian.keySet() + RESET);
		}
	}

	public static void main(String[] args) throws Exception {
		AutoReplyBot bot = new AutoReplyBot();
		Notifier notifier = new Notifier(bot);
		notifier.watchMessagesFile();
	}
}
