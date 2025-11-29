package app;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.Optional; 

import javax.swing.JFrame;
import javax.swing.JOptionPane; 
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import util.StudentDataFetcher; 
import util.StudentDataFetcher.StudentMedicalData; 

public class BPDCClinicApp extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    
    // Reference to the dashboard components
    private final AdminDashboard adminDashboardPanel; 
    private final StudentDashboard studentDashboardPanel; 
    private final BPDCLoginFullScreen loginPanel;

    // Member field to hold the logged-in Student object (for card data access)
    private StudentDashboard.Student currentStudent = null; 
    
    // Getter for the current student object, used by StudentDashboard
    public StudentDashboard.Student getStudent() {
        return currentStudent;
    }

    public BPDCClinicApp() {
        setTitle("BPDC Clinic System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full Screen
        setMinimumSize(new Dimension(800, 600));
        
        // 1. Initialize Panels
        adminDashboardPanel = new AdminDashboard(this); 
        studentDashboardPanel = new StudentDashboard(this); 
        loginPanel = new BPDCLoginFullScreen(this);
        JPanel medicalFormPanel = new MedicalInfoForm(this);
        

        // 2. Add panels to the CardLayout
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(medicalFormPanel, "MedicalForm");
        mainPanel.add(adminDashboardPanel, "AdminDashboard"); 
        mainPanel.add(studentDashboardPanel, "StudentDashboard"); 
        
        add(mainPanel);
        setVisible(true);

        // Start with the login screen
        showLogin();
    }
    
    /**
     * Helper method to build a fully-populated Student object from fetched data.
     */
    private StudentDashboard.Student buildStudentFromMedicalData(StudentMedicalData md) {
        // IMPORTANT: Map all 12 fields from StudentMedicalData (md) to the Student constructor
        return new StudentDashboard.Student(
            md.bitsId,
            md.fullName,
            md.email,
            md.gender,
            md.allergies,
            md.chronicIllnesses,
            md.insuranceType,
            md.bloodType,
            md.mobileNo,
            md.studentTelegramId,
            md.guardianName,
            md.guardianContact
        );
    }
    
    /**
     * Helper method to create a minimal Student object with placeholders for fallback.
     */
    private StudentDashboard.Student buildMinimalStudent(String bitsId, String fullName, String email) {
        // Create student with 9 "N/A" placeholders to match the 12-argument signature
        return new StudentDashboard.Student(
            bitsId, fullName, email, 
            "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A"
        );
    }

    public void showLogin() {
        // Clear the current user session upon logging out
        this.currentStudent = null;
        cardLayout.show(mainPanel, "Login");
        setTitle("BPDC Clinic System - Login");
    }

    public void showMedicalForm() {
        cardLayout.show(mainPanel, "MedicalForm");
        setTitle("BPDC Clinic System - Medical Registration");
    }

    public void showAdminDashboard(AdminDashboard.Staff staff) {
        String alerts = staff.getPendingActionStatus();
        adminDashboardPanel.updateUserDetails(staff.getFullName(), staff.getBitsId(), staff.getEmail(), alerts);
        cardLayout.show(mainPanel, "AdminDashboard");
        setTitle(staff.getDashboardTitle());
    }
    
    /**
     * Staff/Nurse Lookup Method: Attempts to show the student dashboard 
     * by fetching full data from CSV using the BITS ID without password.
     * @param bitsId The BITS ID to lookup.
     */
    public void lookupStudentDashboard(String bitsId) {
        
        // 1. Fetch ALL data from the CSV using the BITS ID
        Optional<StudentMedicalData> medicalDataOpt = 
            StudentDataFetcher.fetchStudentData(bitsId);
        
        if (medicalDataOpt.isPresent()) {
             StudentMedicalData data = medicalDataOpt.get();
             
             // 2. CREATE FULLY POPULATED STUDENT OBJECT using the helper
             StudentDashboard.Student fullStudent = buildStudentFromMedicalData(data);
             
             // 3. Store the fully-populated student object in the app state
             this.currentStudent = fullStudent;
             
             // 4. Update the dashboard UI
             studentDashboardPanel.updateUserDetails(fullStudent); // Pass full object
             cardLayout.show(mainPanel, "StudentDashboard");
             // Update title to show context for the staff member
             setTitle(fullStudent.getDashboardTitle() + " (Viewing Record: " + data.fullName + ")"); 
             
        } else {
            // Data Lookup Failed
            JOptionPane.showMessageDialog(this, "No medical record found for BITS ID: " + bitsId + ". Please check the ID or confirm registration status.", "Record Not Found", JOptionPane.ERROR_MESSAGE);
            // If called from AdminDashboard, this stays on AdminDashboard
            // If called from BPDCLoginFullScreen, the user would be stuck on the login screen (which is fine)
        }
    }
    
    /**
     * Login Flow Method: Initializes student session after successful login 
     * by fetching the full record from the CSV file.
     * @param bitsId The BITS ID of the student who just logged in.
     */
    public void showStudentDashboard(String bitsId) {
        // Use the generic lookup method, which handles fetching data and updating UI
        lookupStudentDashboard(bitsId);
    }
    
    /**
     * Registration Flow Method: Initializes student session using a pre-created Student object
     * from the registration form, bypassing the CSV lookup.
     * This method fixes the call: app.showStudentDashboard(student) from MedicalInfoForm.java.
     * @param loginStudent The newly created Student object.
     */
    public void showStudentDashboard(StudentDashboard.Student loginStudent) {
        // 1. Store the fully-populated student object in the app state
        this.currentStudent = loginStudent;
        
        // 2. Update the dashboard UI
        studentDashboardPanel.updateUserDetails(loginStudent); // Pass full object
        cardLayout.show(mainPanel, "StudentDashboard");
        setTitle(loginStudent.getDashboardTitle());
    }

    public static void main(String[] args) {
        // Run on the Event Dispatch Thread
        SwingUtilities.invokeLater(BPDCClinicApp::new);
    }
}