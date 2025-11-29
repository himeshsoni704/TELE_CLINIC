package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory; 
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import util.CredentialManager;
import util.MedicalDataWriter;

// CHANGED: Extends JPanel instead of JFrame to work with the CardLayout manager
public class MedicalInfoForm extends JPanel { 
    
    // NEW: Reference to the credential manager (Local File I/O)
    private final CredentialManager credentialManager = new CredentialManager();
    // NEW: Reference to the main application manager
    private final BPDCClinicApp app; 
    
    private JComboBox<String> insuranceTypeCombo;
    private JPanel externalInsurancePanel;
    
    // NEW: Member fields to hold references to input components (for data collection)
    // USER INFORMATION FIELDS
    private JTextField fullNameField;
    private JTextField bitsIdField;
    private JPasswordField passwordField;
    private JComboBox<String> genderCombo; 
    private JTextField bitsEmailField;
    private JTextField mobileNumberField;
    private JComboBox<String> mobileCountryCodeCombo; 
    private JTextField whatsappNumberField;
    private JComboBox<String> whatsappCountryCodeCombo;
    private JTextField emiratesIdField;
    private JTextField guardianNameField;
    private JTextField guardianContactField;
    private JComboBox<String> guardianContactCodeCombo;
    private JTextField guardianEmailField;
    private JTextField bloodTypeField;
    private JTextField allergiesField;
    private JTextField chronicIllnessesField;
    private JTextField currentMedicationField;
    private JTextField pastMedicationField;
    
    // Color scheme (UPDATED with soft background color)
    private static final Color BACKGROUND_COLOR = new Color(240, 247, 245);
    private static final Color FIELD_COLOR = new Color(230, 230, 230);
    private static final Color DARK_BLUE = new Color(25, 55, 109);
    private static final Color LIGHT_BORDER = new Color(200, 200, 200);
    
    // --- ADDED COLORS/FONTS FOR BUTTON & TOP BAR ---
    private static final Color BRAND_BLUE = new Color(47, 103, 246);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 16); // UPDATED: Reduced font size
    
    private static final Color BAR_YELLOW = new Color(255, 193, 7);
    private static final Color BAR_BLUE = new Color(100, 149, 237);
    private static final Color BAR_RED = Color.RED;
    // -----------------------------------------------
    
    // CHANGED: Constructor accepts BPDCClinicApp
    public MedicalInfoForm(BPDCClinicApp app) {
        this.app = app; 
        
        setLayout(new BorderLayout()); 
        setBackground(BACKGROUND_COLOR);

        // --- ADDED TOP COLOR BAR (BorderLayout.NORTH) ---
        JPanel topBar = createTopColorBar();
        add(topBar, BorderLayout.NORTH); 
        // ---------------------------
        
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Left Panel - User Information
        JPanel leftPanel = createUserInfoPanel();
        mainPanel.add(leftPanel);
        
        // Right Panel - Medical Information
        JPanel rightPanel = createMedicalInfoPanel();
        mainPanel.add(rightPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // --- ADDED SUBMIT/BACK BUTTON PANEL (BorderLayout.SOUTH) ---
        // Use BorderLayout to align buttons left and right
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);
        // Add horizontal padding and bottom margin
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 50)); 

        // 1. Create Back Button (Left side)
        JButton backButton = new JButton("←");
        styleSquarePrimaryButton(backButton); 
        backButton.addActionListener(e -> app.showLogin());
        
        // Wrapper for Back Button to control alignment (FlowLayout.LEFT is default)
        JPanel backWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backWrapper.setOpaque(false);
        backWrapper.add(backButton);
        bottomPanel.add(backWrapper, BorderLayout.WEST);


        // 2. Create Submit Button (Right side)
        JButton submitButton = new JButton("CREATE"); // UPDATED: Text change
        stylePrimaryButton(submitButton);
        
        // Wrapper for Submit Button to control alignment
        JPanel submitWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        submitWrapper.setOpaque(false);
        submitWrapper.add(submitButton);
        
        // Action listener to collect data, save to CSV, and redirect
        submitButton.addActionListener(e -> {
            boolean success = collectAndSaveData(); 
            
            if (success) {
                // Collect key data to create the Student object for the session
                String fullName = getFieldValue(fullNameField);
                String bitsId = getFieldValue(bitsIdField);
                String email = getFieldValue(bitsEmailField);
                
                // Get new fields for the 12-argument constructor
                String gender = (String) genderCombo.getSelectedItem();
                String gName = getFieldValue(guardianNameField);
                // Combine country code and number for guardian contact
                String gContact = guardianContactCodeCombo.getSelectedItem() + " " + getFieldValue(guardianContactField);
                
                // FIX: Updated to match the new 12-argument constructor with collected data and N/A placeholders
                StudentDashboard.Student student = new StudentDashboard.Student(
                    bitsId, 
                    fullName, 
                    email, 
                    gender, // NEW: Gender
                    "N/A", // Allergies placeholder
                    "N/A", // Chronic Illnesses placeholder
                    "N/A", // Insurance Type placeholder
                    "N/A", // Blood Type placeholder
                    getFieldValue(mobileNumberField), // Mobile No 
                    getFieldValue(whatsappNumberField), // Telegram ID (mapped from WhatsApp field)
                    gName, // NEW: Guardian Name
                    gContact // NEW: Guardian Contact
                );
                
                JOptionPane.showMessageDialog(this, "Registration data saved successfully to medical_registrations.csv!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // FIX: Pass the Student object to the app manager
                app.showStudentDashboard(student); 
            }
        }); 
        
        bottomPanel.add(submitWrapper, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Title - centered and aligned
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel title = new JLabel("USER INFORMATION");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(DARK_BLUE);
        titlePanel.add(title);
        panel.add(titlePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Full Name
        fullNameField = createTextField("Full Name"); // ASSIGNMENT
        panel.add(fullNameField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // BITS ID and Password (side by side)
        JPanel rowIDPass = new JPanel(new GridLayout(1, 2, 15, 0));
        rowIDPass.setBackground(BACKGROUND_COLOR);
        rowIDPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        bitsIdField = createTextField("BITS ID"); // ASSIGNMENT
        rowIDPass.add(bitsIdField);
        passwordField = createPasswordField("Set Account Password"); // NEW ASSIGNMENT
        rowIDPass.add(passwordField);
        panel.add(rowIDPass);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Gender field - Now a dropdown
        genderCombo = createGenderDropdown(); // ASSIGNMENT
        panel.add(genderCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // BITS Email
        bitsEmailField = createTextField("BITS Email"); // ASSIGNMENT
        panel.add(bitsEmailField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Mobile No and WhatsApp No (side by side with country codes)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 15, 0));
        row2.setBackground(BACKGROUND_COLOR);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        // MODIFIED: Use overloaded createPhoneField to capture component references
        row2.add(createPhoneField("Mobile No.", c -> mobileCountryCodeCombo = c, f -> mobileNumberField = f));
        row2.add(createPhoneField("Telegram ID ", c -> whatsappCountryCodeCombo = c, f -> whatsappNumberField = f)); // Updated placeholder text
        panel.add(row2);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Date of Birth 
        panel.add(createDOBPanel()); 
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Emirates ID
        emiratesIdField = createTextField("Emirates ID"); // ASSIGNMENT
        panel.add(emiratesIdField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Guardian's Name
        guardianNameField = createTextField("Guardian's Name"); // ASSIGNMENT
        panel.add(guardianNameField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Guardian's Active Contact No
        // MODIFIED: Use overloaded createPhoneField to capture component references
        panel.add(createPhoneField("Guardian's  contact no.", c -> guardianContactCodeCombo = c, f -> guardianContactField = f));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Guardian's Email
        guardianEmailField = createTextField("Guardian's email address"); // ASSIGNMENT
        panel.add(guardianEmailField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        return panel;
    }
    
    private JPanel createMedicalInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Title - centered and aligned
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel title = new JLabel("MEDICAL INFORMATION");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(DARK_BLUE);
        titlePanel.add(title);
        panel.add(titlePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Blood Type
        bloodTypeField = createTextField("Blood Type"); // ASSIGNMENT
        panel.add(bloodTypeField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Allergies
        allergiesField = createTextField("Allergies (N/A if none)"); // ASSIGNMENT
        panel.add(allergiesField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Chronic illnesses/Diseases
        chronicIllnessesField = createTextField("Chronic illnesses/Diseases (N/A if none)"); // ASSIGNMENT
        panel.add(chronicIllnessesField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Current medication
        currentMedicationField = createTextField("Current medication"); // ASSIGNMENT
        panel.add(currentMedicationField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Past medication/Treatments
        pastMedicationField = createTextField("Past medication /Treatments"); // ASSIGNMENT
        panel.add(pastMedicationField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Insurance Type Dropdown
        panel.add(createInsuranceDropdown());
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // External Insurance Panel (initially hidden)
        externalInsurancePanel = createExternalInsurancePanel();
        externalInsurancePanel.setVisible(false);
        panel.add(externalInsurancePanel);
        
        return panel;
    }

    // NEW METHOD: Creates a styled JPasswordField with placeholder logic
    private JPasswordField createPasswordField(String placeholder) {
        final JPasswordField field = new JPasswordField();
        
        // Apply styling similar to JTextField
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        field.setPreferredSize(new Dimension(300, 50));
        field.setBackground(FIELD_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BORDER),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // Placeholder setup
        field.setFont(new Font("Arial", Font.ITALIC, 13));
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        char defaultEchoChar = field.getEchoChar();
        field.setEchoChar((char) 0); // Show placeholder text plainly

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setFont(new Font("Arial", Font.PLAIN, 13));
                    field.setEchoChar(defaultEchoChar); // Start masking
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DARK_BLUE, 2),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            public void focusLost(FocusEvent e) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setFont(new Font("Arial", Font.ITALIC, 13));
                    field.setText(placeholder);
                    field.setEchoChar((char) 0); // Revert to placeholder style
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LIGHT_BORDER),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        return field;
    }
    
    // MODIFIED: createTextField now explicitly returns JTextField
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        field.setPreferredSize(new Dimension(300, 50));
        field.setBackground(FIELD_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BORDER),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        field.setFont(new Font("Arial", Font.ITALIC, 13));
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setFont(new Font("Arial", Font.PLAIN, 13));
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DARK_BLUE, 2),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setFont(new Font("Arial", Font.ITALIC, 13));
                    field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LIGHT_BORDER),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        
        return field;
    }
    
    // NEW METHOD: Creates the Gender Dropdown
    private JComboBox<String> createGenderDropdown() {
        String[] options = {"Gender (Select)", "Male", "Female", "Other"};
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        // Use existing styling method
        styleComboBox(combo); 
        // Ensure the initial selection is the placeholder
        combo.setSelectedIndex(0); 
        return combo;
    }
    
    // NEW OVERLOAD: createPhoneField to capture component references using Consumers
    private JPanel createPhoneField(String placeholder, Consumer<JComboBox<String>> codeConsumer, Consumer<JTextField> fieldConsumer) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Country code dropdown with +971 as default (Dubai)
        String[] countryCodes = {"+971", "+1", "+44", "+91", "+86", "+81", "+33", "+49", "+61", "+7", 
                                 "+20", "+27", "+30", "+31", "+32", "+34", "+39", "+41", "+43", "+45", 
                                 "+46", "+47", "+48", "+52", "+55", "+60", "+61", "+62", "+63", "+64", 
                                 "+65", "+66", "+82", "+84", "+90", "+92", "+93", "+94", "+95", "+98"};
        JComboBox<String> countryCode = new JComboBox<>(countryCodes);
        countryCode.setSelectedItem("+971");
        countryCode.setPreferredSize(new Dimension(85, 50));
        countryCode.setBackground(FIELD_COLOR);
        countryCode.setFont(new Font("Arial", Font.PLAIN, 13));
        countryCode.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER));
        
        countryCode.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                countryCode.setBorder(BorderFactory.createLineBorder(DARK_BLUE, 2));
            }
            public void focusLost(FocusEvent e) {
                countryCode.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER));
            }
        });
        
        // Phone number field (reuses createTextField logic)
        JTextField field = createTextField(placeholder);
        
        // Assign references back to instance variables
        codeConsumer.accept(countryCode);
        fieldConsumer.accept(field);

        panel.add(countryCode, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDOBPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel label = new JLabel("D.O.B :");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setPreferredSize(new Dimension(80, 50));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        
        JPanel dropdownPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        dropdownPanel.setBackground(BACKGROUND_COLOR);
        
        // Day dropdown
        String[] days = new String[32];
        days[0] = "DD";
        for (int i = 1; i <= 31; i++) {
            days[i] = String.format("%02d", i);
        }
        JComboBox<String> dayCombo = new JComboBox<>(days);
        styleComboBox(dayCombo);
        
        // Month dropdown
        String[] months = {"MM", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        styleComboBox(monthCombo);
        
        // Year dropdown
        String[] years = new String[106];
        years[0] = "YYYY";
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 1; i <= 105; i++) {
            years[i] = String.valueOf(currentYear - i + 1);
        }
        JComboBox<String> yearCombo = new JComboBox<>(years);
        styleComboBox(yearCombo);
        
        dropdownPanel.add(dayCombo);
        dropdownPanel.add(monthCombo);
        dropdownPanel.add(yearCombo);
        
        panel.add(label, BorderLayout.WEST);
        panel.add(dropdownPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(FIELD_COLOR);
        combo.setFont(new Font("Arial", Font.ITALIC, 13));
        combo.setForeground(Color.GRAY);
        combo.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER));
        
        combo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                combo.setBorder(BorderFactory.createLineBorder(DARK_BLUE, 2));
            }
            public void focusLost(FocusEvent e) {
                combo.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER));
            }
        });
        
        combo.addActionListener(e -> {
            // Check if the selected index is not the placeholder (index 0)
            if (combo.getSelectedIndex() > 0) {
                combo.setFont(new Font("Arial", Font.PLAIN, 13));
                combo.setForeground(Color.BLACK);
            } else {
                combo.setFont(new Font("Arial", Font.ITALIC, 13));
                combo.setForeground(Color.GRAY);
            }
        });
    }
    
    private JPanel createInsuranceDropdown() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        String[] insuranceOptions = {"Insurance Type", "Institute Insurance", "Personal/External Insurance"};
        insuranceTypeCombo = new JComboBox<>(insuranceOptions);
        insuranceTypeCombo.setBackground(FIELD_COLOR);
        insuranceTypeCombo.setFont(new Font("Arial", Font.ITALIC, 13));
        insuranceTypeCombo.setForeground(Color.GRAY);
        insuranceTypeCombo.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER));
        
        insuranceTypeCombo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                insuranceTypeCombo.setBorder(BorderFactory.createLineBorder(DARK_BLUE, 2));
            }
            public void focusLost(FocusEvent e) {
                insuranceTypeCombo.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER));
            }
        });
        
        insuranceTypeCombo.addActionListener(e -> {
            int selected = insuranceTypeCombo.getSelectedIndex();
            if (selected == 2) { // Personal/External Insurance
                externalInsurancePanel.setVisible(true);
                insuranceTypeCombo.setFont(new Font("Arial", Font.PLAIN, 13));
                insuranceTypeCombo.setForeground(Color.BLACK);
            } else {
                externalInsurancePanel.setVisible(false);
                if (selected > 0) {
                    insuranceTypeCombo.setFont(new Font("Arial", Font.PLAIN, 13));
                    insuranceTypeCombo.setForeground(Color.BLACK);
                }
            }
            revalidate();
            repaint();
        });
        
        panel.add(insuranceTypeCombo, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createExternalInsurancePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(DARK_BLUE, 2),
            "External Insurance Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            DARK_BLUE
        ));
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createTextField("Insurance Company Name"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createTextField("Policy Number"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createTextField("Policy Holder Name"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createPhoneField("Insurance Contact Number", c -> {}, f -> {})); // Ignore assignment here
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        return panel;
    }
    
    // NEW METHOD: Creates the Yellow/Blue/Red bar for the top (from previous request)
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
    
    // MODIFIED METHOD: Styles the Primary button (from previous request)
    private void stylePrimaryButton(JButton btn) {
        btn.setFont(BUTTON_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(BRAND_BLUE);
        btn.setPreferredSize(new Dimension(200, 50)); 
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(BRAND_BLUE.darker()); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(BRAND_BLUE); }
        });
    }
    
    /**
     * Styles the Back Button as a square, primary-colored button.
     */
    private void styleSquarePrimaryButton(JButton btn) {
        btn.setFont(new Font(Font.DIALOG, Font.BOLD, 28)); 
        btn.setForeground(Color.WHITE); 
        btn.setBackground(BRAND_BLUE); 
        btn.setPreferredSize(new Dimension(60, 60)); // 60x60
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(BRAND_BLUE.darker()); } // Darken on hover
            public void mouseExited(MouseEvent evt) { btn.setBackground(BRAND_BLUE); }
        });
    }

    /**
     * Safely retrieves the text from a text/password field, handling placeholders.
     */
    private String getFieldValue(JTextField field) {
        if (field == null) return "N/A";
        // Check if field is a JPasswordField
        if (field instanceof JPasswordField) {
            char[] password = ((JPasswordField) field).getPassword();
            String value = new String(password).trim();
            // Clear the sensitive password array immediately
            Arrays.fill(password, ' '); 
            
            // Placeholder check for JPasswordField
            if (value.isEmpty() || value.equals("Set Account Password")) {
                return "N/A";
            }
            return value;
        }
        
        // Logic for standard JTextField
        boolean isPlaceholder = field.getFont().isItalic(); 
        String value = field.getText().trim();
        
        // List of placeholders to check against
        String[] commonPlaceholders = {"Full Name", "BITS ID", "Gender", "BITS Email", "Mobile No.", "Telegram no.", "Emirates ID", "Guardian's Name", "Guardian's  contact no.", "Guardian's email address", "Blood Type", "Allergies (N/A if none)", "Chronic illnesses/Diseases (N/A if none)", "Current medication", "Past medication /Treatments"}; // Added new placeholder

        // Check for placeholder indicators or emptiness
        if (value.isEmpty() || isPlaceholder) {
            for (String ph : commonPlaceholders) {
                 if (value.equals(ph)) return "N/A";
            }
        }
        return value.equals("YYYY") || value.equals("MM") || value.equals("DD") ? "N/A" : value;
    }

    /**
     * Helper method to validate mandatory fields.
     * @return null if all mandatory fields are filled, or an error message string otherwise.
     */
    private String validateMandatoryFields() {
        // Define mandatory fields and their display names
        Map<JTextField, String> mandatoryFields = new LinkedHashMap<>();
        
        // User Information
        mandatoryFields.put(fullNameField, "Full Name");
        mandatoryFields.put(bitsIdField, "BITS ID");
        mandatoryFields.put(bitsEmailField, "BITS Email");
        mandatoryFields.put(mobileNumberField, "Mobile Number");
        
        // Guardian Information (essential contact info)
        mandatoryFields.put(guardianNameField, "Guardian's Name");
        mandatoryFields.put(guardianContactField, "Guardian's Contact Number");

        // Medical Information
        mandatoryFields.put(bloodTypeField, "Blood Type");
        
        for (Map.Entry<JTextField, String> entry : mandatoryFields.entrySet()) {
            if (getFieldValue(entry.getKey()).equals("N/A")) {
                return entry.getValue() + " must be filled out.";
            }
        }
        
        // Special check for JPasswordField
        if (getFieldValue(passwordField).equals("N/A")) {
            return "Account Password must be set.";
        }
        
        // Special check for Gender dropdown (index 0 is the placeholder "Gender (Select)")
        if (genderCombo.getSelectedIndex() == 0) {
            return "Gender must be selected.";
        }
        
        return null; // All checks passed
    }

    /**
     * Collects all data, registers the user credentials, and saves the medical data.
     * @return true if both credential and medical data saving were successful, false otherwise.
     */
    private boolean collectAndSaveData() {
        // 1. Validate all mandatory fields first
        String validationError = validateMandatoryFields();
        
        if (validationError != null) {
            // Demonstrates Exception Handling / User Feedback
            JOptionPane.showMessageDialog(this, "Submission Failed: " + validationError, "Data Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 2. Collect required credentials (BITS ID and Password)
        String bitsId = getFieldValue(bitsIdField);
        String password = getFieldValue(passwordField); 
        
        // 3. Register Credentials first (Mandatory)
        // Demonstrates Exception Handling / User Feedback if registration fails
        boolean credsSaved = credentialManager.addCredential(bitsId, password, "Student");
        if (!credsSaved) {
             JOptionPane.showMessageDialog(this, "Failed to save login credentials. This BITS ID might already be registered.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        
        // 4. Collect remaining medical and personal data
        String fullName = getFieldValue(fullNameField);
        // MODIFIED: Retrieve gender from JComboBox
        String gender = (String) genderCombo.getSelectedItem(); 
        String email = getFieldValue(bitsEmailField);
        
        // Combine country code and number
        String mobile = mobileCountryCodeCombo != null ? mobileCountryCodeCombo.getSelectedItem() + " " + getFieldValue(mobileNumberField) : "N/A";
        // Mapped WhatsApp field input to TelegramID column
        String telegramId = whatsappCountryCodeCombo != null ? whatsappCountryCodeCombo.getSelectedItem() + " " + getFieldValue(whatsappNumberField) : "N/A";
        
        String bloodType = getFieldValue(bloodTypeField);
        String allergies = getFieldValue(allergiesField);
        String chronicIllnesses = getFieldValue(chronicIllnessesField);
        String insuranceType = (String) insuranceTypeCombo.getSelectedItem();
        
        // NEW: Guardian Data (These are collected but NOT part of the original 10-column CSV)
        String guardianName = getFieldValue(guardianNameField);
        String guardianContact = guardianContactCodeCombo != null ? guardianContactCodeCombo.getSelectedItem() + " " + getFieldValue(guardianContactField) : "N/A";
        // Note: Guardian Email is collected but skipped here as per previous CSV structure analysis.
        
        
        // 5. Build the data array in the order defined by the CSV_HEADER (10 columns only):
        // FullName,BITS_ID,Gender,BITS_Email,MobileNo,TelegramID,BloodType,Allergies,ChronicIllnesses,InsuranceType
        // NOTE: If you update MedicalDataWriter.java to save Guardian fields, update CSV_HEADER there.
        // For now, we manually append the guardian data as extra columns to allow fetcher to retrieve it.
        String[] data = {
            fullName, 
            bitsId, 
            gender, 
            email,
            mobile, 
            telegramId, 
            bloodType, 
            allergies, 
            chronicIllnesses, 
            insuranceType,
            guardianName, // EXTRA COLUMN 11
            guardianContact, // EXTRA COLUMN 12
        };
        
        // 6. Call the writer utility to save medical data
        return MedicalDataWriter.writeDataToCsv(data);
    }
}