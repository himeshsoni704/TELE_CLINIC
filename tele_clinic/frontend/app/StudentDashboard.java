package app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import core.ClinicUser;
import core.IClinicOperations;
import util.EmergencyLogWriter;

/**
 * StudentDashboard - LIGHT THEME with Emergency Button and Logout
 * FIXED: Added back button to Prescription page and fixed prescription display
 * KEY FIX: Implemented prescription data loading from frontend/prescriptions.txt
 */
public class StudentDashboard extends JPanel implements IClinicOperations {

    // --- NEW: Prescription Data Structure ---
    private static class Prescription {
        String date;
        String medication;
        String dosage;
        String doctor;

        public Prescription(String date, String medication, String dosage, String doctor) {
            this.date = date;
            this.medication = medication;
            this.dosage = dosage;
            this.doctor = doctor;
        }
    }

    // --- Data Storage ---
    private List<Prescription> prescriptions = new ArrayList<>();
    private static final String PRESCRIPTION_FILE_PATH = "C://Users//Kusha//OneDrive//Desktop//tele_clinic//frontend//prescriptions.txt";
    // ----------------------

    public static class Student extends ClinicUser {
        private String gender;
        private String allergies;
        private String chronicIllnesses;
        private String insuranceType;
        private String bloodType;
        private String mobileNo;
        private String studentTelegramId;
        private String guardianName;
        private String guardianContact;

        public Student(String bitsId, String fullName, String email, String gender,
                String allergies, String chronicIllnesses, String insuranceType,
                String bloodType, String mobileNo, String studentTelegramId,
                String guardianName, String guardianContact) {
            super(bitsId, fullName, email, "Student");
            this.gender = gender;
            this.allergies = allergies;
            this.chronicIllnesses = chronicIllnesses;
            this.insuranceType = insuranceType;
            this.bloodType = bloodType;
            this.mobileNo = mobileNo;
            this.studentTelegramId = studentTelegramId;
            this.guardianName = guardianName;
            this.guardianContact = guardianContact;
        }

        @Override
        public String getDashboardTitle() {
            return "BPDC Clinic Student Portal";
        }

        public String getAlertsStatus() {
            if (this.allergies.equalsIgnoreCase("N/A") && this.chronicIllnesses.equalsIgnoreCase("N/A")) {
                return "No known critical alerts.";
            } else {
                return "Alert: Review medical history.";
            }
        }

        public String getGender() { return gender; }
        public String getInsuranceType() { return insuranceType; }
        public String getBloodType() { return bloodType; }
        public String getAllergies() { return allergies; }
        public String getChronicIllnesses() { return chronicIllnesses; }
        public String getStudentTelegramId() { return studentTelegramId; }
        public String getMobileNo() { return mobileNo; }
        public String getGuardianName() { return guardianName; }
        public String getGuardianContact() { return guardianContact; }
    }

    private final BPDCClinicApp app;
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);

    // --- LIGHT THEME Color Palette ---
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color DARK_BLUE = new Color(33, 41, 52);
    private static final Color BRAND_BLUE = new Color(25, 118, 210);
    private static final Color LIGHT_BORDER = new Color(200, 210, 220);
    private static final Color BAR_YELLOW = new Color(255, 193, 7);
    private static final Color BAR_BLUE = new Color(30, 136, 229);
    private static final Color BAR_RED = new Color(244, 67, 54);

    // --- Fonts ---
    private static final Font HEADER_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font USER_INFO_NAME_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font USER_INFO_ID_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font CARD_CONTENT_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font BODY_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);

    private static class Clinic {
        String name;
        String location;
        String type;

        public Clinic(String name, String location, String type) {
            this.name = name;
            this.location = location;
            this.type = type;
        }
    }

    private JLabel dashboardNameLabel;
    private JLabel studentIdLabel;
    private JLabel studentEmailLabel;
    private String currentBitsId = "N/A";
    private String currentFullName = "Student User";

    private String lastLogin = "N/A";
    private String insuranceProvider = "N/A";
    
    // Hardcoded prescription data removed, will be loaded from file
    // private String lastPrescriptionDate = "22-11-2024";
    // private String lastPrescriptionMedication = "Paracetamol 500mg - Twice a day";

    private List<Clinic> nearbyClinics = new ArrayList<>();

    public StudentDashboard(BPDCClinicApp app) {
        this.app = app;
        // Load data on startup
        loadPrescriptions();

        nearbyClinics.add(new Clinic("Fakeeh University Hospital", "Dubai Silicon Oasis", "Hospital"));
        nearbyClinics.add(new Clinic("Aster Clinic", "Dubai Silicon Oasis", "Clinic"));
        nearbyClinics.add(new Clinic("Health Connect Poly Clinic", "Academic City", "Clinic"));

        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        JPanel topBar = createTopColorBar();
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeaderPanel();
        centerWrapper.add(headerPanel, BorderLayout.NORTH);

        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        
        // Initial dashboard setup with potentially empty data
        mainContentPanel.add(createDashboardPanel(null), "Dashboard"); 
        mainContentPanel.add(createPrescriptionHistoryPanel(), "Prescription History");
        mainContentPanel.add(createPlaceholderPanel("Appointments/Booking Content"), "Appointments");
        mainContentPanel.add(createPlaceholderPanel("Insurance Details Content"), "Insurance Details");

        centerWrapper.add(mainContentPanel, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        cardLayout.show(mainContentPanel, "Dashboard");
    }

    // --- NEW: Load Prescription Data from File ---
    private void loadPrescriptions() {
        prescriptions.clear();
        
        // Ensure currentBitsId is set before attempting to filter
        if (currentBitsId.equals("N/A") || currentBitsId.isEmpty()) {
            System.err.println("Cannot load prescriptions: Student ID is unknown. Update user details first.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(PRESCRIPTION_FILE_PATH))) {
            
            String line;
            // 🔑 FIX 1: Read and skip the header line (StudentID,Timestamp,StaffID,StaffName,Medication,Notes)
            br.readLine(); 

            while ((line = br.readLine()) != null) {
                
                // For simplicity, strip all quotes added by the writer and split by comma.
                String noQuotesLine = line.replace("\"", "");
                String[] parts = noQuotesLine.split(",");
                
                // 🔑 FIX 2: Check for at least 6 parts
                if (parts.length >= 6) {
                    
                    String studentId = parts[0].trim();
                    String date = parts[1].trim();     // Maps to Timestamp
                    String staffName = parts[3].trim(); // Maps to Doctor's name
                    String medication = parts[4].trim(); 
                    String dosage = parts[5].trim();   // Maps to Notes/Instructions

                    // 🔑 FIX 3: Filter by the currently logged-in student ID
                    if (studentId.equals(currentBitsId)) {
                        prescriptions.add(new Prescription(
                            date,       // Use Timestamp as the date
                            medication, 
                            dosage,     // Use Notes as the dosage/instructions
                            staffName   // Use StaffName as the doctor
                        ));
                    }
                }
            }
            // Sort by date (optional, but good practice for history) - reverse order for newest first
            // Note: Simple String comparison won't work perfectly for dates unless they are YYYY-MM-DD
            Collections.reverse(prescriptions); 
            
            System.out.println("Loaded " + prescriptions.size() + " prescriptions for ID: " + currentBitsId);

        } catch (IOException e) {
            System.err.println("Error reading prescription file: " + e.getMessage());
            // Optionally show a dialog to the user or log the error
            // JOptionPane.showMessageDialog(this, "Could not load prescription history.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // ---------------------------------------------


    public void updateUserDetails(Student student) {
        this.currentBitsId = student.getBitsId();
        this.currentFullName = student.getFullName();

        // Reload prescriptions if they might be specific to the logged-in user (optional, depending on design)
        loadPrescriptions(); 

        SwingUtilities.invokeLater(() -> {
            // Update header
            dashboardNameLabel.setText("Welcome, " + student.getFullName());
            studentIdLabel.setText("Student ID: " + student.getBitsId());
            studentEmailLabel.setText(student.getEmail());

            // KEY FIX: Pass student object directly to dashboard creation
            mainContentPanel.removeAll();
            mainContentPanel.add(createDashboardPanel(student), "Dashboard");
            mainContentPanel.add(createPrescriptionHistoryPanel(), "Prescription History");
            mainContentPanel.add(createPlaceholderPanel("Appointments/Booking Content"), "Appointments");
            mainContentPanel.add(createPlaceholderPanel("Insurance Details Content"), "Insurance Details");
            cardLayout.show(mainContentPanel, "Dashboard");

            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        });
    }

    @Override
    public void updateUserDetails(String fullName, String bitsId, String email, String alerts) {
        // This method is primarily for the Staff/Admin class, but kept for interface compatibility
        this.currentBitsId = bitsId;
        this.currentFullName = fullName;
        SwingUtilities.invokeLater(() -> {
            if (dashboardNameLabel != null) dashboardNameLabel.setText("Welcome, " + fullName);
            if (studentIdLabel != null) studentIdLabel.setText("Student ID: " + bitsId);
            if (studentEmailLabel != null) studentEmailLabel.setText(email);
        });
    }

    private JPanel createTopColorBar() {
        JPanel barPanel = new JPanel();
        barPanel.setLayout(new GridLayout(1, 3, 0, 0));
        barPanel.setPreferredSize(new Dimension(0, 30));

        JPanel yellowZone = new JPanel();
        yellowZone.setBackground(BAR_YELLOW);

        JPanel blueZone = new JPanel();
        blueZone.setBackground(BAR_BLUE);

        JPanel redZone = new JPanel();
        redZone.setBackground(BAR_RED);

        barPanel.add(yellowZone);
        barPanel.add(blueZone);
        barPanel.add(redZone);

        return barPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(50, 0));
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(LIGHT_BORDER, 2),
            BorderFactory.createEmptyBorder(20, 50, 20, 50)
        ));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("BPDC Clinic Student Portal");
        titleLabel.setFont(HEADER_TITLE_FONT);
        titleLabel.setForeground(BRAND_BLUE);
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel userInfo = new JPanel(new GridLayout(3, 1));
        userInfo.setOpaque(false);
        userInfo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        dashboardNameLabel = new JLabel("Welcome, Student User");
        dashboardNameLabel.setFont(USER_INFO_NAME_FONT);
        dashboardNameLabel.setForeground(DARK_BLUE);
        userInfo.add(dashboardNameLabel);

        studentEmailLabel = new JLabel("email@dubai.bits-pilani.ac.in");
        studentEmailLabel.setFont(BODY_FONT_PLAIN);
        studentEmailLabel.setForeground(new Color(100, 110, 120));
        userInfo.add(studentEmailLabel);

        studentIdLabel = new JLabel("Student ID: N/A");
        studentIdLabel.setFont(USER_INFO_ID_FONT);
        studentIdLabel.setForeground(BRAND_BLUE);
        userInfo.add(studentIdLabel);

        leftPanel.add(userInfo, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.WEST);

        // RIGHT SIDE: Emergency Button + Logout Button
        JButton emergencyBtn = new JButton("🚨 EMERGENCY CALL");
        emergencyBtn.setBackground(BAR_RED);
        emergencyBtn.setForeground(Color.WHITE);
        emergencyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emergencyBtn.setPreferredSize(new Dimension(200, 50));
        emergencyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emergencyBtn.setFocusPainted(false);
        emergencyBtn.setBorderPainted(false);
        emergencyBtn.setOpaque(true);
        emergencyBtn.addActionListener(e -> handleEmergencyCall());

        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.setBackground(BAR_YELLOW.darker());
        logoutBtn.setForeground(DARK_BLUE);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setPreferredSize(new Dimension(150, 50));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.addActionListener(e -> app.showLogin());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.add(emergencyBtn);
        btnWrapper.add(logoutBtn);

        panel.add(btnWrapper, BorderLayout.EAST);

        return panel;
    }

    @Override
    public void handleEmergencyCall() {
        int result = JOptionPane.showConfirmDialog(this,
                "You are about to place an EMERGENCY ALERT.\nThis will instantly notify clinic staff and log the event.\nProceed?",
                "CONFIRM EMERGENCY ALERT",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (EmergencyLogWriter.logAndNotifyEmergency(currentBitsId, currentFullName)) {
                JOptionPane.showMessageDialog(this,
                        "EMERGENCY ALERT SENT!\nClinic staff and wardens have been notified via Telegram for user: " + currentBitsId,
                        "ALERT CONFIRMED",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "EMERGENCY ALERT FAILED TO LOG OR NOTIFY! Please check file permissions/network.",
                        "LOG/NOTIFY ERROR",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createDashboardPanel(Student student) {
        JPanel dashboard = new JPanel(new BorderLayout(0, 20));
        dashboard.setOpaque(false);

        // TOP ROW: Cards (3/4) + Clinics (1/4)
        JPanel topRowGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        topRowGrid.setOpaque(false);

        topRowGrid.add(createMedicalInfoCard(student));
        topRowGrid.add(createGuardianInfoCard(student));
        topRowGrid.add(createPrescriptionCard());
        topRowGrid.add(createClinicsCard());

        // BOTTOM ROW: Medical Alerts Detail (Full Width)
        JPanel medicalAlertsPanel = createMedicalAlertsDetailPanel(student);

        dashboard.add(topRowGrid, BorderLayout.NORTH);
        dashboard.add(medicalAlertsPanel, BorderLayout.CENTER);

        return dashboard;
    }

    private JPanel createMedicalAlertsDetailPanel(Student student) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BAR_RED, 2),
                    "Medical Alerts Detail (Allergies & Chronic Illnesses)",
                    0,
                    0,
                    CARD_TITLE_FONT,
                    BRAND_BLUE),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setBackground(CARD_BACKGROUND);

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        contentPanel.setOpaque(false);

        // Use passed student object
        String allergies = (student != null) ? student.getAllergies() : "N/A";
        String chronicIllnesses = (student != null) ? student.getChronicIllnesses() : "N/A";

        JLabel allergiesLabel = new JLabel("Allergies: " + (allergies.equalsIgnoreCase("N/A") || allergies.isEmpty() ? "None reported." : allergies));
        allergiesLabel.setFont(BODY_FONT_PLAIN);
        allergiesLabel.setForeground(DARK_BLUE);
        contentPanel.add(allergiesLabel);

        JLabel chronicLabel = new JLabel("Chronic Illnesses/Diseases: " + (chronicIllnesses.equalsIgnoreCase("N/A") || chronicIllnesses.isEmpty() ? "None reported." : chronicIllnesses));
        chronicLabel.setFont(BODY_FONT_PLAIN);
        chronicLabel.setForeground(DARK_BLUE);
        contentPanel.add(chronicLabel);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDataCard(String title, String content, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BORDER, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(bgColor);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE_FONT);
        titleLabel.setForeground(BRAND_BLUE);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel contentLabel = new JLabel("<html><p style='width: 150px;'>" + content + "</p></html>");
        contentLabel.setFont(CARD_CONTENT_FONT);
        card.add(contentLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMedicalInfoCard(Student student) {
        JPanel card = createDataCard("Medical Summary", "", new Color(220, 237, 255));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Use passed student object instead of app.getStudent()
        String gender = (student != null) ? student.getGender() : "N/A";
        String bloodType = (student != null) ? student.getBloodType() : "N/A";
        String insuranceType = (student != null) ? student.getInsuranceType() : "N/A";

        JLabel genderLabel = new JLabel("Gender: " + gender);
        genderLabel.setFont(BODY_FONT_PLAIN);
        genderLabel.setForeground(DARK_BLUE);
        contentPanel.add(genderLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JLabel bloodLabel = new JLabel("Blood Type: " + bloodType);
        bloodLabel.setFont(BODY_FONT_PLAIN);
        bloodLabel.setForeground(DARK_BLUE);
        contentPanel.add(bloodLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JLabel insuranceLabel = new JLabel("Insurance: " + insuranceType);
        insuranceLabel.setFont(BODY_FONT_PLAIN);
        insuranceLabel.setForeground(new Color(100, 110, 120));
        contentPanel.add(insuranceLabel);

        card.remove(card.getComponent(1));
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createGuardianInfoCard(Student student) {
        JPanel card = createDataCard("Guardian Contact", "", new Color(200, 255, 200));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Use passed student object
        String guardianName = (student != null) ? student.getGuardianName() : "N/A";
        String guardianContact = (student != null) ? student.getGuardianContact() : "N/A";

        JLabel nameLabel = new JLabel("Name: " + guardianName);
        nameLabel.setFont(BODY_FONT_PLAIN);
        nameLabel.setForeground(DARK_BLUE);
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JLabel contactLabel = new JLabel("Contact No: " + guardianContact);
        contactLabel.setFont(BODY_FONT_PLAIN);
        contactLabel.setForeground(DARK_BLUE);
        contentPanel.add(contactLabel);

        card.remove(card.getComponent(1));
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createPrescriptionCard() {
        JPanel card = createDataCard("Last Prescription", "", new Color(255, 240, 210));

        JPanel contentPanel = new JPanel(new GridLayout(3, 1));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // --- FIX: Use loaded data ---
        Prescription lastRx = prescriptions.isEmpty() ? null : prescriptions.get(0);
        String date = (lastRx != null) ? lastRx.date : "N/A";
        String medication = (lastRx != null) ? lastRx.medication : "No recent prescription";
        String dosage = (lastRx != null) ? lastRx.dosage + " (" + lastRx.doctor + ")" : "";
        // ---------------------------

        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(CARD_CONTENT_FONT);
        dateLabel.setForeground(DARK_BLUE);
        contentPanel.add(dateLabel);

        JLabel medLabel = new JLabel(medication);
        medLabel.setFont(BODY_FONT_PLAIN);
        medLabel.setForeground(new Color(100, 110, 120));
        contentPanel.add(medLabel);

        JButton historyBtn = new JButton("View Full History →");
        historyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBtn.setForeground(BRAND_BLUE);
        historyBtn.setBorderPainted(false);
        historyBtn.setContentAreaFilled(false);
        historyBtn.setFocusPainted(false);
        historyBtn.setHorizontalAlignment(SwingConstants.LEFT);
        historyBtn.setFont(BODY_FONT_PLAIN);
        historyBtn.addActionListener(e -> cardLayout.show(mainContentPanel, "Prescription History"));

        contentPanel.add(historyBtn);

        card.remove(card.getComponent(1));
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createClinicsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(DARK_BLUE, 2),
                    "Nearby Medical Facilities",
                    0,
                    0,
                    CARD_TITLE_FONT,
                    BRAND_BLUE),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        card.setBackground(CARD_BACKGROUND);
        card.setPreferredSize(new Dimension(300, 250));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        List<Clinic> visibleClinics = nearbyClinics.subList(0, Math.min(nearbyClinics.size(), 2));

        for (Clinic clinic : visibleClinics) {
            JPanel item = new JPanel(new BorderLayout(10, 0));
            Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, LIGHT_BORDER);
            Border itemPadding = BorderFactory.createEmptyBorder(5, 0, 5, 0);
            item.setBorder(new CompoundBorder(bottomLine, itemPadding));
            item.setOpaque(false);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(clinic.name);
            nameLabel.setFont(BODY_FONT_PLAIN);
            textPanel.add(nameLabel);

            JLabel locationLabel = new JLabel(clinic.location + " (" + clinic.type + ")");
            locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            locationLabel.setForeground(new Color(100, 110, 120));
            textPanel.add(locationLabel);

            item.add(textPanel, BorderLayout.CENTER);

            JLabel icon = new JLabel("📍");
            icon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            icon.setForeground(BRAND_BLUE);
            item.add(icon, BorderLayout.EAST);

            listPanel.add(item);
        }

        JLabel viewMoreLabel = new JLabel("...View Full List", SwingConstants.CENTER);
        viewMoreLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        viewMoreLabel.setForeground(BRAND_BLUE);
        listPanel.add(Box.createVerticalStrut(5));
        listPanel.add(viewMoreLabel);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    // 🔑 NEW: Prescription History Panel with Back Button
    private JPanel createPrescriptionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // TOP: Back Button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setForeground(BRAND_BLUE);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(mainContentPanel, "Dashboard"));

        topPanel.add(backBtn);
        panel.add(topPanel, BorderLayout.NORTH);

        // CENTER: Prescription Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel titleLabel = new JLabel("Prescription History");
        titleLabel.setFont(HEADER_TITLE_FONT);
        titleLabel.setForeground(BRAND_BLUE);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));

        // --- FIX: Dynamically add prescription items from the loaded list ---
        if (prescriptions.isEmpty()) {
            JLabel emptyLabel = new JLabel("No prescription history found.", SwingConstants.CENTER);
            emptyLabel.setFont(BODY_FONT_PLAIN);
            emptyLabel.setForeground(DARK_BLUE);
            contentPanel.add(emptyLabel);
        } else {
            for (Prescription rx : prescriptions) {
                JPanel rxPanel = createPrescriptionItemPanel(rx.date, rx.medication, rx.dosage, rx.doctor);
                contentPanel.add(rxPanel);
                contentPanel.add(Box.createVerticalStrut(20));
            }
        }
        // ------------------------------------------------------------------

        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // 🔑 Helper: Create prescription item card
    private JPanel createPrescriptionItemPanel(String date, String medication, String dosage, String doctor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(LIGHT_BORDER, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setBackground(CARD_BACKGROUND);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel dateLabel = new JLabel("Date: " + date);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setForeground(DARK_BLUE);
        leftPanel.add(dateLabel);

        JLabel medLabel = new JLabel("Medication: " + medication);
        medLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        medLabel.setForeground(DARK_BLUE);
        leftPanel.add(medLabel);

        JLabel dosageLabel = new JLabel("Dosage: " + dosage);
        dosageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dosageLabel.setForeground(new Color(100, 110, 120));
        leftPanel.add(dosageLabel);

        JLabel doctorLabel = new JLabel("Doctor: " + doctor);
        doctorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        doctorLabel.setForeground(new Color(100, 110, 120));
        leftPanel.add(doctorLabel);

        card.add(leftPanel, BorderLayout.CENTER);

        JLabel badge = new JLabel("Rx");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 20));
        badge.setForeground(BRAND_BLUE);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(60, 80));
        card.add(badge, BorderLayout.EAST);

        return card;
    }

    private JPanel createPlaceholderPanel(String contentText) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Placeholder: " + contentText, SwingConstants.CENTER));
        panel.setBackground(new Color(255, 255, 240));
        return panel;
    }
}