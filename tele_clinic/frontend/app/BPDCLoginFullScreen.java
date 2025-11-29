package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import core.ClinicUser;
import util.CredentialManager;


// CHANGED: Extends JPanel instead of JFrame
public class BPDCLoginFullScreen extends JPanel { 

    private final BPDCClinicApp app; 
    private final CredentialManager credentialManager = new CredentialManager(); 
    
    // UI components needed for logic
    private JTextField usernameField;
    
    // --- Placeholder Strings (Static now, focused on Student ID) ---
    private static final String STUDENT_PLACEHOLDER = "BITS ID (20XXAXPSXXXU or Nurse ID)";
    private static final String PASSWORD_PLACEHOLDER = "Password";

    // --- Color Palette ---
    private static final Color BRAND_BLUE = new Color(47, 103, 246);
    private static final Color BRAND_RED = Color.RED;
    private static final Color LIGHT_GRAY_BORDER = new Color(220, 220, 220);
    private static final Color PLACEHOLDER_TEXT = new Color(160, 160, 160);
    private static final Color FORM_BACKGROUND = new Color(240, 247, 245); 

    // Bottom Bar Colors
    private static final Color BOTTOM_YELLOW = new Color(255, 193, 7);
    private static final Color BOTTOM_CORNFLOWER = new Color(100, 149, 237);
    private static final Color BOTTOM_RED = Color.RED;

    // Fonts
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 32);
    private static final Font INPUT_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font LINK_FONT = new Font("Arial", Font.BOLD, 14);

    // CHANGED: Constructor accepts BPDCClinicApp
    public BPDCLoginFullScreen(BPDCClinicApp app) {
        this.app = app; 
        
        setLayout(new BorderLayout()); 
        setBackground(FORM_BACKGROUND); 

        // --- 2. Responsive Header (Top) ---
        ImageHeaderPanel headerPanel = new ImageHeaderPanel();
        headerPanel.setPreferredSize(new Dimension(100, 300)); 
        add(headerPanel, BorderLayout.NORTH);

        // --- 3. Centered Form Content (Center) ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(FORM_BACKGROUND); 

        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setBackground(FORM_BACKGROUND); 
        formBox.setPreferredSize(new Dimension(400, 500));

        // -- Form Elements --
        JLabel titleLabel = new JLabel("BPDC Clinic Login"); // Updated Title
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Fields
        usernameField = createPlaceholderTextField(STUDENT_PLACEHOLDER); 
        JPasswordField passwordField = createPlaceholderPasswordField(PASSWORD_PLACEHOLDER);
        
        JButton continueButton = new JButton("Login"); // Changed text to Login
        stylePrimaryButton(continueButton);
        
        // Action listener for "Login" button -> LOGIN VERIFICATION
        continueButton.addActionListener(e -> attemptLogin(usernameField, passwordField));

        JButton createAccountBtn = createLinkButton("Create an account", BRAND_BLUE);
        
        // Action listener for "Create an account" button -> Redirection only
        createAccountBtn.addActionListener(e -> app.showMedicalForm());
        
        JButton forgotPasswordBtn = createLinkButton("Forgot Password ?", BRAND_RED);

        // Add to Form Box
        formBox.add(Box.createVerticalGlue());
        formBox.add(titleLabel);
        formBox.add(Box.createVerticalStrut(50));
        formBox.add(usernameField);
        formBox.add(Box.createVerticalStrut(20));
        formBox.add(passwordField);
        formBox.add(Box.createVerticalStrut(30));
        formBox.add(continueButton);
        formBox.add(Box.createVerticalStrut(30));
        formBox.add(createAccountBtn);
        formBox.add(Box.createVerticalStrut(10));
        formBox.add(forgotPasswordBtn);
        formBox.add(Box.createVerticalGlue());

        centerWrapper.add(formBox);
        add(centerWrapper, BorderLayout.CENTER);

        // --- 4. Bottom Colored Bar (South) ---
        JPanel bottomBarPanel = new JPanel();
        bottomBarPanel.setLayout(new GridLayout(1, 3, 0, 0));
        bottomBarPanel.setPreferredSize(new Dimension(0, 30));

        JPanel yellowZone = new JPanel();
        yellowZone.setBackground(BOTTOM_YELLOW);
        JPanel blueZone = new JPanel();
        blueZone.setBackground(BOTTOM_CORNFLOWER);
        JPanel redZone = new JPanel();
        redZone.setBackground(BOTTOM_RED);

        bottomBarPanel.add(yellowZone);
        bottomBarPanel.add(blueZone);
        bottomBarPanel.add(redZone);

        add(bottomBarPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Handles the login attempt, verifying credentials against the CSV file.
     * Demonstrates: Polymorphism (creating concrete subclass objects and passing them to the main app).
     */
    private void attemptLogin(JTextField bitsIdField, JPasswordField passwordField) {
        String bitsId = bitsIdField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());
        
        // 1. Basic input validation (check if placeholder text is still present)
        if (bitsId.isEmpty() || bitsId.equals(STUDENT_PLACEHOLDER) || password.isEmpty() || password.equals(PASSWORD_PLACEHOLDER)) {
            JOptionPane.showMessageDialog(this, "Please enter your ID and password to log in.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Verify credentials using the manager
        Optional<String> verifiedRole = credentialManager.verifyCredentials(bitsId, password);

        if (verifiedRole.isPresent()) {
            // Successful Login
            String actualRole = verifiedRole.get();
            ClinicUser user; // Declare user as the base abstract class type (Polymorphism)
            
            // Generate mock data for the user object
            String email = bitsId.toLowerCase().contains("nurse") || bitsId.toLowerCase().contains("admin") ? 
                           bitsId.toLowerCase() + "@bpdcclinic.com" : 
                           bitsId.toLowerCase().replace("u", "") + "@dubai.bits-pilani.ac.in";
            
            if (actualRole.equals("Student")) {
                // Refactored: Directly call the main app to fetch the full record using the BITS ID,
                // avoiding the creation of an intermediate Student object with "N/A" placeholders.
                app.showStudentDashboard(bitsId); 
            } else if (actualRole.equals("Nurse") || actualRole.equals("Admin")) { 
                // Instantiates Staff object (Polymorphism)
                user = new AdminDashboard.Staff(bitsId, "Clinic Staff", email, bitsId);
                // We pass the concrete object.
                app.showAdminDashboard((AdminDashboard.Staff)user);
            } else {
                 JOptionPane.showMessageDialog(this, "Unknown user role found.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
            
        } else {
            // Invalid Login
            JOptionPane.showMessageDialog(this, "Invalid ID or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    class ImageHeaderPanel extends JPanel {
        private BufferedImage image;

        public ImageHeaderPanel() {
            try {
                 // Using a placeholder image URL, as local files might not be accessible
                File url = new File("app/image_1.png");
                 image = ImageIO.read(url);

            } catch (IOException ex) {
                System.err.println("--- IMAGE LOAD ERROR ---");
                System.err.println("Error: " + ex.getMessage());
                
                image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.LIGHT_GRAY); 
                g.drawString("LOAD FAIL", 10, 50);
                g.dispose();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // --- Helper Methods (Styling) ---

    private JTextField createPlaceholderTextField(String placeholder) {
        final JTextField field = new JTextField(placeholder);
        styleInputField(field);
        field.setForeground(PLACEHOLDER_TEXT);
        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                 if (field.getText().equals(STUDENT_PLACEHOLDER) || field.getText().equals(PASSWORD_PLACEHOLDER)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(PLACEHOLDER_TEXT);
                    field.setText(STUDENT_PLACEHOLDER);
                }
            }
        });
        return field;
    }

    private JPasswordField createPlaceholderPasswordField(String placeholder) {
        final JPasswordField field = new JPasswordField(placeholder);
        styleInputField(field);
        field.setForeground(PLACEHOLDER_TEXT);
        char defaultEchoChar = field.getEchoChar();
        field.setEchoChar((char) 0);
        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar(defaultEchoChar);
                }
            }
            public void focusLost(FocusEvent e) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setForeground(PLACEHOLDER_TEXT);
                    field.setText(placeholder);
                    field.setEchoChar((char) 0);
                }
            }
        });
        return field;
    }

    private void styleInputField(JTextField field) {
        field.setFont(INPUT_FONT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        field.setPreferredSize(new Dimension(350, 50));
        Border line = new LineBorder(LIGHT_GRAY_BORDER, 1);
        Border margin = new EmptyBorder(5, 15, 5, 15);
        field.setBorder(new CompoundBorder(line, margin));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(BUTTON_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(BRAND_BLUE);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btn.setPreferredSize(new Dimension(350, 55));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(BRAND_BLUE.darker()); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(BRAND_BLUE); }
        });
    }

    private JButton createLinkButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(LINK_FONT);
        btn.setForeground(color);
        btn.setBackground(FORM_BACKGROUND);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}