package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class to read student medical and personal details from the CSV file.
 * The expected CSV format (12 columns):
 * FullName(0),BITS_ID(1),Gender(2),BITS_Email(3),MobileNo(4),TelegramID(5),BloodType(6),Allergies(7),ChronicIllnesses(8),InsuranceType(9),GuardianName(10),GuardianContact(11)
 */
public class StudentDataFetcher {

	private static final String FILE_NAME = "medical_registrations.csv";
	// BITS_ID is at index 1 (FullName is at 0)
	private static final int BITS_ID_INDEX = 1;

	/**
	 * Data class to hold the retrieved student details.
	 */
	public static class StudentMedicalData {
		// Core User Info
		public final String fullName; // 0
		public final String bitsId; // 1
		public final String gender; // 2
		public final String email; // 3

		// Medical/Contact Details
		public final String mobileNo; // 4 (Mobile contact)
		public final String studentTelegramId; // 5
		public final String bloodType; // 6
		public final String allergies; // 7
		public final String chronicIllnesses; // 8
		public final String insuranceType; // 9

		// Guardian Details
		public final String guardianName; // 10
		public final String guardianContact; // 11

		public StudentMedicalData(String[] parts) {
			// FIX: Refined cleaning function to correctly handle quotes and normalize "N/A"
			Function<String, String> clean = s -> {
				if (s == null) return "N/A";
				s = s.trim();

				// CRITICAL FIX: Strip surrounding double quotes aggressively.
				if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
					s = s.substring(1, s.length() - 1);
				}
				s = s.trim();

				// Normalize result to "N/A" if empty or contains only whitespace
				if (s.isEmpty() || s.equalsIgnoreCase("N/A")) {
					return "N/A";
				}
				return s;
			};

			// Safely retrieve data using getPartSafely which checks array bounds
			Function<Integer, String> getPartSafely = index -> {
				if (index >= 0 && index < parts.length) {
					return clean.apply(parts[index]);
				}
				return "N/A";
			};

			this.fullName = getPartSafely.apply(0);
			this.bitsId = getPartSafely.apply(1);
			this.gender = getPartSafely.apply(2);
			this.email = getPartSafely.apply(3);

			this.mobileNo = getPartSafely.apply(4);
			this.studentTelegramId = getPartSafely.apply(5);
			this.bloodType = getPartSafely.apply(6);
			this.allergies = getPartSafely.apply(7);
			this.chronicIllnesses = getPartSafely.apply(8);
			this.insuranceType = getPartSafely.apply(9);

			this.guardianName = getPartSafely.apply(10);
			this.guardianContact = getPartSafely.apply(11);

			// DEBUG: Print loaded data
			System.out.println("[StudentDataFetcher] ✓ Loaded data for: " + this.fullName);
			System.out.println("   - Gender: " + this.gender);
			System.out.println("   - Blood Type: " + this.bloodType);
			System.out.println("   - Allergies: " + this.allergies);
			System.out.println("   - Guardian: " + this.guardianName + " (" + this.guardianContact + ")");
		}
	}

	/**
	 * Fetches the medical data record for a given BITS ID.
	 * @param bitsId The BITS ID to search for.
	 * @return An Optional containing the StudentMedicalData object if found.
	 */
	public static Optional<StudentMedicalData> fetchStudentData(String bitsId) {
		System.out.println("[StudentDataFetcher] Searching for BITS ID: " + bitsId);

		try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
			String line;
			int lineNum = 0;

			// Skip header
			line = br.readLine();
			if (line != null) {
				lineNum++;
				System.out.println("[StudentDataFetcher] ✓ Skipped header: " + line);
			}

			// Read data lines
			while ((line = br.readLine()) != null) {
				lineNum++;

				// Use split with -1 limit to keep trailing empty strings
				String[] parts = line.split(",", -1);

				if (parts.length < 2) {
					System.out.println("[StudentDataFetcher] Line " + lineNum + ": Skipped (insufficient columns)");
					continue;
				}

				// Get the BITS ID part and clean it for comparison
				String storedBitsIdPart = parts[BITS_ID_INDEX];
				String storedBitsId = storedBitsIdPart.trim();

				// Apply the same quote stripping logic for robust comparison
				if (storedBitsId.length() >= 2 && storedBitsId.startsWith("\"") && storedBitsId.endsWith("\"")) {
					storedBitsId = storedBitsId.substring(1, storedBitsId.length() - 1).trim();
				}

				System.out.println("[StudentDataFetcher] Line " + lineNum + ": Checking BITS_ID=" + storedBitsId);

				// Case-insensitive comparison
				if (storedBitsId.equalsIgnoreCase(bitsId.trim())) {
					System.out.println("[StudentDataFetcher] ✓ MATCH FOUND! Creating StudentMedicalData...");
					return Optional.of(new StudentMedicalData(parts));
				}
			}

			System.err.println("[StudentDataFetcher] ✗ BITS ID NOT FOUND: " + bitsId);

		} catch (IOException e) {
			System.err.println("[StudentDataFetcher] ✗ ERROR reading CSV: " + e.getMessage());
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
