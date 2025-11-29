package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import core.ClinicUser;
import core.IClinicOperations;

/**
 * AdminDashboard - LIGHT THEME
 * Modern, sleek UI with compact tabs and improved readability
 */
public class AdminDashboard extends JPanel implements IClinicOperations {

	public static class Staff extends ClinicUser {
		private String employeeId;

		public Staff(String bitsId, String fullName, String email, String employeeId) {
			super(bitsId, fullName, email, "Staff");
			this.employeeId = employeeId;
		}

		@Override
		public String getDashboardTitle() {
			return "BPDC Clinic Nurse Portal";
		}

		public String getPendingActionStatus() {
			return "3 Pending appointments to review.";
		}
	}

	private final BPDCClinicApp app;

	// --- LIGHT THEME Color Palette ---
	private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
	private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
	private static final Color DARK_TEXT = new Color(33, 41, 52);
	private static final Color BRAND_BLUE = new Color(25, 118, 210);
	private static final Color BRAND_PURPLE = new Color(142, 68, 173);
	private static final Color LIGHT_BORDER = new Color(200, 210, 220);
	private static final Color GRAY = new Color(120, 130, 140);
	private static final Color TEXT_LIGHT = new Color(50, 60, 70);

	private static final Color BAR_YELLOW = new Color(255, 193, 7);
	private static final Color BAR_BLUE = new Color(30, 136, 229);
	private static final Color BAR_RED = new Color(244, 67, 54);

	// Tab colors - HIGH CONTRAST LIGHT
	private static final Color TAB_INACTIVE_BG = new Color(240, 242, 245);
	private static final Color TAB_INACTIVE_TEXT = new Color(100, 110, 120);
	private static final Color TAB_ACTIVE_BG = new Color(255, 255, 255);
	private static final Color TAB_ACTIVE_TEXT = new Color(25, 118, 210);

	// --- Fonts ---
	private static final Font HEADER_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
	private static final Font USER_INFO_NAME_FONT = new Font("Segoe UI", Font.BOLD, 22);
	private static final Font USER_INFO_ID_FONT = new Font("Segoe UI", Font.PLAIN, 15);
	private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);
	private static final Font BODY_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);
	private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 13);
	private static final Font TAB_FONT = new Font("Segoe UI", Font.BOLD, 13);

	private JLabel dashboardNameLabel;
	private JLabel dashboardIdLabel;
	private JLabel emailLabel;
	private JLabel medicalAlertsLabel;
	private String currentBitsId = "N/A";
	private String currentFullName = "Clinic Staff";

	private JTextField prescriptionStudentIdField;
	private JTextField prescriptionMedicationField;
	private JTextArea prescriptionNotesArea;

	private static final String MESSAGES_FILE = "...\\..\\backend\\oops\\messages.txt";
	private JButton notificationIcon;
	private int unreadMessagesCount = 0;
	private long lastMsgSize = 0;

	private JTabbedPane mainTabbedPane;

	public AdminDashboard(BPDCClinicApp app) {
		this.app = app;
		setLayout(new BorderLayout());
		setBackground(BACKGROUND_COLOR);

		JPanel topBar = createTopColorBar();
		add(topBar, BorderLayout.NORTH);

		JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
		centerWrapper.setBackground(BACKGROUND_COLOR);
		centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JPanel headerPanel = createHeaderPanel();
		centerWrapper.add(headerPanel, BorderLayout.NORTH);

		mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		mainTabbedPane.setBackground(BACKGROUND_COLOR);
		mainTabbedPane.setForeground(DARK_TEXT);
		mainTabbedPane.setFont(TAB_FONT);
		mainTabbedPane.setUI(new BasicTabbedPaneUI() {
			@Override
			protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 50;
			}

			@Override
			protected int calculateTabWidth(int tabPlacement, int tabIndex, java.awt.FontMetrics metrics) {
				return Math.max(160, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
			}

			@Override
			protected void paintTabBackground(java.awt.Graphics g, int tabPlacement, int tabIndex, 
					int x, int y, int w, int h, boolean isSelected) {
				if (isSelected) {
					g.setColor(TAB_ACTIVE_BG);
				} else {
					g.setColor(TAB_INACTIVE_BG);
				}
				g.fillRect(x, y, w, h);
			}

			@Override
			protected void paintTabBorder(java.awt.Graphics g, int tabPlacement, int tabIndex, 
					int x, int y, int w, int h, boolean isSelected) {
				g.setColor(LIGHT_BORDER);
				g.drawRect(x, y, w, h);
			}
		});

		mainTabbedPane.addChangeListener(e -> {
			mainTabbedPane.revalidate();
			mainTabbedPane.repaint();
		});

		mainTabbedPane.addTab("📊 Dashboard", createDashboardContent());
		mainTabbedPane.addTab("💊 Prescription", createPrescriptionPanel());
		mainTabbedPane.addTab("🔍 Student Lookup", createStudentLookupPanel());

		centerWrapper.add(mainTabbedPane, BorderLayout.CENTER);
		add(centerWrapper, BorderLayout.CENTER);

		updateUserDetails("Clinic Staff", "N/A", "N/A", "N/A");
		startMessageWatcher();
	}

	@Override
	public void updateUserDetails(String fullName, String bitsId, String email, String alerts) {
		this.currentBitsId = bitsId;
		this.currentFullName = fullName;

		SwingUtilities.invokeLater(() -> {
			dashboardNameLabel.setText("Welcome, " + fullName);
			dashboardIdLabel.setText("ID: " + bitsId);

			if (emailLabel != null)
				emailLabel.setText(email);
			if (medicalAlertsLabel != null)
				medicalAlertsLabel.setText(alerts);

			if (alerts.contains("Alert:") || alerts.contains("Pending")) {
				medicalAlertsLabel.setForeground(BAR_RED);
			} else if (medicalAlertsLabel != null) {
				medicalAlertsLabel.setForeground(BRAND_BLUE);
			}
		});
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout(20, 0));
		panel.setBackground(CARD_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LIGHT_BORDER, 2),
				BorderFactory.createEmptyBorder(15, 20, 15, 20)));

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setOpaque(false);

		JLabel titleLabel = new JLabel("BPDC Clinic Nurse Portal");
		titleLabel.setFont(HEADER_TITLE_FONT);
		titleLabel.setForeground(BRAND_BLUE);
		leftPanel.add(titleLabel, BorderLayout.NORTH);

		JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 5));
		userInfo.setOpaque(false);
		userInfo.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

		dashboardNameLabel = new JLabel("Welcome, ");
		dashboardNameLabel.setFont(USER_INFO_NAME_FONT);
		dashboardNameLabel.setForeground(DARK_TEXT);
		userInfo.add(dashboardNameLabel);

		dashboardIdLabel = new JLabel("ID: ");
		dashboardIdLabel.setFont(USER_INFO_ID_FONT);
		dashboardIdLabel.setForeground(BRAND_BLUE);
		userInfo.add(dashboardIdLabel);

		leftPanel.add(userInfo, BorderLayout.CENTER);

		JButton logoutBtn = new JButton("LOGOUT");
		logoutBtn.setBackground(BAR_RED);
		logoutBtn.setForeground(Color.WHITE);
		logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		logoutBtn.setPreferredSize(new Dimension(120, 50));
		logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		logoutBtn.setFocusPainted(false);
		logoutBtn.setBorderPainted(false);
		logoutBtn.setOpaque(true);
		logoutBtn.addActionListener(e -> app.showLogin());

		notificationIcon = new JButton("✉ (0)");
		notificationIcon.setBackground(BRAND_BLUE);
		notificationIcon.setForeground(Color.WHITE);
		notificationIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
		notificationIcon.setPreferredSize(new Dimension(100, 50));
		notificationIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
		notificationIcon.setFocusPainted(false);
		notificationIcon.setBorderPainted(false);
		notificationIcon.setOpaque(true);
		notificationIcon.addActionListener(e -> {
			JOptionPane.showMessageDialog(this, "You have " + unreadMessagesCount + " unread messages.",
					"Messages", JOptionPane.INFORMATION_MESSAGE);
			unreadMessagesCount = 0;
			updateNotificationIcon();
		});

		JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		btnWrapper.setOpaque(false);
		btnWrapper.add(notificationIcon);
		btnWrapper.add(logoutBtn);

		panel.add(leftPanel, BorderLayout.CENTER);
		panel.add(btnWrapper, BorderLayout.EAST);

		return panel;
	}

	@Override
	public void handleEmergencyCall() {
	}

	private JTextField createPlaceholderField(final String placeholder) {
		final JTextField field = createStyledInputField(placeholder);
		field.setFont(new Font("Segoe UI", Font.ITALIC, 12));
		field.setForeground(GRAY);

		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (field.getText().equals(placeholder)) {
					field.setText("");
					field.setFont(INPUT_FONT);
					field.setForeground(DARK_TEXT);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (field.getText().isEmpty()) {
					field.setText(placeholder);
					field.setFont(new Font("Segoe UI", Font.ITALIC, 12));
					field.setForeground(GRAY);
				}
			}
		});
		return field;
	}

	private JTextArea createPlaceholderTextArea(final String placeholder) {
		final JTextArea area = createStyledTextArea(placeholder);
		area.setFont(new Font("Segoe UI", Font.ITALIC, 12));
		area.setForeground(GRAY);

		area.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (area.getText().equals(placeholder)) {
					area.setText("");
					area.setFont(INPUT_FONT);
					area.setForeground(DARK_TEXT);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (area.getText().isEmpty()) {
					area.setText(placeholder);
					area.setFont(new Font("Segoe UI", Font.ITALIC, 12));
					area.setForeground(GRAY);
				}
			}
		});
		return area;
	}

	private JTextField createStyledInputField(String placeholder) {
		JTextField field = new JTextField(placeholder);
		field.setFont(INPUT_FONT);
		field.setPreferredSize(new Dimension(250, 38));
		field.setBackground(Color.WHITE);
		field.setForeground(DARK_TEXT);
		field.setCaretColor(DARK_TEXT);
		field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LIGHT_BORDER, 2),
				BorderFactory.createEmptyBorder(6, 10, 6, 10)));
		return field;
	}

	private JTextArea createStyledTextArea(String placeholder) {
		JTextArea area = new JTextArea(placeholder);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(INPUT_FONT);
		area.setBackground(Color.WHITE);
		area.setForeground(DARK_TEXT);
		area.setCaretColor(DARK_TEXT);
		area.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LIGHT_BORDER, 2),
				BorderFactory.createEmptyBorder(6, 10, 6, 10)));
		return area;
	}

	private JPanel createPrescriptionPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBackground(BACKGROUND_COLOR);

		JLabel title = new JLabel("💊 Add Prescription");
		title.setFont(HEADER_TITLE_FONT);
		title.setForeground(BRAND_BLUE);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(title);
		form.add(Box.createRigidArea(new Dimension(0, 25)));

		JLabel idLabel = new JLabel("Student BITS ID:");
		idLabel.setFont(CARD_TITLE_FONT);
		idLabel.setForeground(DARK_TEXT);
		idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(idLabel);

		prescriptionStudentIdField = createPlaceholderField(
				currentBitsId.equals("N/A") ? "20XXAXPSXXXU" : currentBitsId);
		prescriptionStudentIdField.setMaximumSize(new Dimension(450, 38));
		prescriptionStudentIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(prescriptionStudentIdField);
		form.add(Box.createRigidArea(new Dimension(0, 15)));

		JLabel medLabel = new JLabel("Medication / Diagnosis:");
		medLabel.setFont(CARD_TITLE_FONT);
		medLabel.setForeground(DARK_TEXT);
		medLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(medLabel);

		prescriptionMedicationField = createPlaceholderField("e.g., Paracetamol, 500mg");
		prescriptionMedicationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
		prescriptionMedicationField.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(prescriptionMedicationField);
		form.add(Box.createRigidArea(new Dimension(0, 15)));

		JLabel notesLabel = new JLabel("Additional Instructions:");
		notesLabel.setFont(CARD_TITLE_FONT);
		notesLabel.setForeground(DARK_TEXT);
		notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(notesLabel);

		prescriptionNotesArea = createPlaceholderTextArea("Enter detailed instructions...");
		prescriptionNotesArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		JScrollPane scrollPane = new JScrollPane(prescriptionNotesArea);
		scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER, 2));
		form.add(scrollPane);
		form.add(Box.createRigidArea(new Dimension(0, 25)));

		JButton submitBtn = new JButton("✓ SAVE PRESCRIPTION");
		submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		submitBtn.setBackground(BAR_BLUE);
		submitBtn.setForeground(Color.WHITE);
		submitBtn.setPreferredSize(new Dimension(250, 45));
		submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
		submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		submitBtn.setFocusPainted(false);
		submitBtn.setBorderPainted(false);
		submitBtn.setOpaque(true);
		submitBtn.addActionListener(e -> handlePrescriptionSubmission());
		form.add(submitBtn);

		panel.add(form, BorderLayout.NORTH);
		return panel;
	}

	private void handlePrescriptionSubmission() {
		String defaultStudentId = currentBitsId.equals("N/A") ? "20XXAXPSXXXU" : currentBitsId;
		String studentId = prescriptionStudentIdField.getText().trim();
		String medication = prescriptionMedicationField.getText().trim();
		String notes = prescriptionNotesArea.getText().trim();
		String staffId = currentBitsId;
		String staffName = currentFullName;

		if (studentId.isEmpty() || studentId.equals("20XXAXPSXXXU") || studentId.equals(defaultStudentId)) {
			studentId = defaultStudentId;
		}

		if (studentId.equals("20XXAXPSXXXU") || medication.isEmpty() || medication.equals("e.g., Paracetamol, 500mg")) {
			JOptionPane.showMessageDialog(this, "Student BITS ID and Medication are required.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (notes.equals("Enter detailed instructions...")) {
			notes = "N/A";
		}

		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String[] data = { studentId, timestamp, staffId, staffName, medication, notes.isEmpty() ? "N/A" : notes };

		boolean success = simulateWritePrescription(data);
		if (success) {
			JOptionPane.showMessageDialog(this, "Prescription saved for " + studentId + ".", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			prescriptionMedicationField.setText("e.g., Paracetamol, 500mg");
			prescriptionNotesArea.setText("Enter detailed instructions...");
		} else {
			JOptionPane.showMessageDialog(this, "Failed to save prescription.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private boolean simulateWritePrescription(String[] data) {
		final String FILE_NAME = "prescriptions.txt";
		try (java.io.FileWriter fw = new java.io.FileWriter(FILE_NAME, true);
				java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {
			java.io.File file = new java.io.File(FILE_NAME);
			if (file.length() == 0) {
				pw.println("StudentID,Timestamp,StaffID,StaffName,Medication,Notes");
			}
			String entry = String.join(",", data);
			pw.println(entry);
			return true;
		} catch (IOException e) {
			System.err.println("Error writing prescription: " + e.getMessage());
			return false;
		}
	}

	private void updateNotificationIcon() {
		SwingUtilities.invokeLater(() -> {
			notificationIcon.setText("✉ (" + unreadMessagesCount + ")");
			if (unreadMessagesCount > 0) {
				notificationIcon.setBackground(BAR_RED);
				notificationIcon.setToolTipText(unreadMessagesCount + " new messages");
			} else {
				notificationIcon.setBackground(BRAND_BLUE);
				notificationIcon.setToolTipText("No new messages");
			}
		});
	}

	private void showLiveNotification(String message) {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(this, "New Message:\n" + message, "Notification",
					JOptionPane.INFORMATION_MESSAGE);
			unreadMessagesCount++;
			updateNotificationIcon();
		});
	}

	private void startMessageWatcher() {
		new Thread(() -> {
			try {
				Path filePath = Paths.get(MESSAGES_FILE);
				Path dir = filePath.getParent();
				if (dir == null) {
					System.err.println("Error: Parent directory not found.");
					return;
				}

				WatchService watchService = FileSystems.getDefault().newWatchService();
				dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

				File msgFile = filePath.toFile();
				if (msgFile.exists()) {
					lastMsgSize = msgFile.length();
				}

				while (true) {
					WatchKey key;
					try {
						key = watchService.take();
					} catch (InterruptedException ex) {
						return;
					}

					for (WatchEvent<?> event : key.pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
							File changedFile = dir.resolve((Path) event.context()).toFile();
							if (changedFile.getAbsolutePath().equals(msgFile.getAbsolutePath())) {
								long newSize = changedFile.length();
								if (newSize > lastMsgSize) {
									try (RandomAccessFile raf = new RandomAccessFile(changedFile, "r")) {
										raf.seek(lastMsgSize);
										String line;
										while ((line = raf.readLine()) != null) {
											if (!line.trim().isEmpty()) {
												showLiveNotification(line.trim());
											}
										}
										lastMsgSize = newSize;
									} catch (Exception e) {
										System.err.println("Error reading new message: " + e.getMessage());
									}
								}
							}
						}
					}
					key.reset();
				}
			} catch (Exception e) {
				System.err.println("Message Watcher Error: " + e.getMessage());
			}
		}, "MessageWatcherThread").start();
	}

	private JPanel createDashboardContent() {
		JPanel dashboard = new JPanel(new BorderLayout(0, 15));
		dashboard.setOpaque(false);
		dashboard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel lookupPanel = createStudentLookupCardPanel();
		dashboard.add(lookupPanel, BorderLayout.NORTH);

		JPanel cardStack = new JPanel();
		cardStack.setLayout(new BoxLayout(cardStack, BoxLayout.Y_AXIS));
		cardStack.setOpaque(false);

		cardStack.add(createCompactEmailCard());
		cardStack.add(Box.createVerticalStrut(12));
		cardStack.add(createCompactMedicalAlertsCard());
		cardStack.add(Box.createVerticalStrut(12));
		cardStack.add(createCompactClinicsCard());

		JScrollPane scrollPane = new JScrollPane(cardStack);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBackground(BACKGROUND_COLOR);
		scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
		dashboard.add(scrollPane, BorderLayout.CENTER);

		return dashboard;
	}

	private JPanel createStudentLookupCardPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
		panel.setBackground(CARD_BACKGROUND);
		panel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LIGHT_BORDER, 2),
				BorderFactory.createEmptyBorder(12, 12, 12, 12)));
		panel.setPreferredSize(new Dimension(0, 60));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

		JLabel title = new JLabel("🔍 Quick Lookup:");
		title.setFont(CARD_TITLE_FONT);
		title.setForeground(DARK_TEXT);
		panel.add(title);

		JTextField bitsIdField = createStyledInputField("Enter BITS ID");
		bitsIdField.setPreferredSize(new Dimension(180, 38));
		panel.add(bitsIdField);

		JButton lookupButton = new JButton("► SEARCH");
		lookupButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lookupButton.setBackground(BRAND_BLUE);
		lookupButton.setForeground(Color.WHITE);
		lookupButton.setPreferredSize(new Dimension(110, 38));
		lookupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lookupButton.setFocusPainted(false);
		lookupButton.setBorderPainted(false);
		lookupButton.setOpaque(true);
		lookupButton.addActionListener(e -> {
			String bitsId = bitsIdField.getText().trim();
			if (bitsId.isEmpty() || bitsId.equals("Enter BITS ID")) {
				JOptionPane.showMessageDialog(panel, "Enter a valid BITS ID.", "Error", JOptionPane.WARNING_MESSAGE);
				return;
			}
			app.lookupStudentDashboard(bitsId);
			SwingUtilities.invokeLater(() -> {
				mainTabbedPane.setSelectedIndex(1);
				prescriptionStudentIdField.setText(bitsId);
				if (prescriptionStudentIdField.getFont().isItalic()) {
					prescriptionStudentIdField.setFont(INPUT_FONT);
					prescriptionStudentIdField.setForeground(DARK_TEXT);
				}
			});
		});
		panel.add(lookupButton);

		return panel;
	}

	private JPanel createCompactEmailCard() {
		JPanel card = new JPanel(new BorderLayout(12, 0));
		card.setBackground(CARD_BACKGROUND);
		card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LIGHT_BORDER, 2),
				BorderFactory.createEmptyBorder(12, 12, 12, 12)));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

		JLabel titleLabel = new JLabel("📧 BITS Email");
		titleLabel.setFont(CARD_TITLE_FONT);
		titleLabel.setForeground(BRAND_BLUE);
		titleLabel.setPreferredSize(new Dimension(150, 55));
		card.add(titleLabel, BorderLayout.WEST);

		emailLabel = new JLabel("N/A");
		emailLabel.setFont(BODY_FONT_PLAIN);
		emailLabel.setForeground(DARK_TEXT);
		card.add(emailLabel, BorderLayout.CENTER);

		return card;
	}

	private JPanel createCompactMedicalAlertsCard() {
		JPanel card = new JPanel(new BorderLayout(12, 0));
		card.setBackground(CARD_BACKGROUND);
		card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LIGHT_BORDER, 2),
				BorderFactory.createEmptyBorder(12, 12, 12, 12)));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

		JLabel titleLabel = new JLabel("⚠️ Medical Alerts");
		titleLabel.setFont(CARD_TITLE_FONT);
		titleLabel.setForeground(BAR_RED);
		titleLabel.setPreferredSize(new Dimension(150, 55));
		card.add(titleLabel, BorderLayout.WEST);

		medicalAlertsLabel = new JLabel("None");
		medicalAlertsLabel.setFont(BODY_FONT_PLAIN);
		medicalAlertsLabel.setForeground(DARK_TEXT);
		card.add(medicalAlertsLabel, BorderLayout.CENTER);

		return card;
	}

	private JPanel createCompactClinicsCard() {
		JPanel card = new JPanel(new BorderLayout(12, 0));
		card.setBackground(CARD_BACKGROUND);
		card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LIGHT_BORDER, 2),
				BorderFactory.createEmptyBorder(12, 12, 12, 12)));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

		JLabel titleLabel = new JLabel("🏥 Active Clinic");
		titleLabel.setFont(CARD_TITLE_FONT);
		titleLabel.setForeground(BRAND_PURPLE);
		titleLabel.setPreferredSize(new Dimension(150, 55));
		card.add(titleLabel, BorderLayout.WEST);

		JLabel contentLabel = new JLabel("BPDC Main Clinic");
		contentLabel.setFont(BODY_FONT_PLAIN);
		contentLabel.setForeground(DARK_TEXT);
		card.add(contentLabel, BorderLayout.CENTER);

		return card;
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

	private JPanel createStudentLookupPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBackground(BACKGROUND_COLOR);

		JLabel title = new JLabel("🔍 Student Record Lookup");
		title.setFont(HEADER_TITLE_FONT);
		title.setForeground(BRAND_BLUE);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(title);
		form.add(Box.createRigidArea(new Dimension(0, 25)));

		JLabel idLabel = new JLabel("Enter BITS ID:");
		idLabel.setFont(CARD_TITLE_FONT);
		idLabel.setForeground(DARK_TEXT);
		idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(idLabel);

		JTextField lookupField = createPlaceholderField("2024A7PS0255U");
		lookupField.setMaximumSize(new Dimension(450, 38));
		lookupField.setAlignmentX(Component.LEFT_ALIGNMENT);
		form.add(lookupField);
		form.add(Box.createRigidArea(new Dimension(0, 25)));

		JButton searchBtn = new JButton("► VIEW RECORD");
		searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		searchBtn.setBackground(BRAND_BLUE);
		searchBtn.setForeground(Color.WHITE);
		searchBtn.setPreferredSize(new Dimension(250, 45));
		searchBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		searchBtn.setFocusPainted(false);
		searchBtn.setBorderPainted(false);
		searchBtn.setOpaque(true);
		searchBtn.addActionListener(e -> {
			String bitsId = lookupField.getText().trim();
			if (bitsId.isEmpty() || bitsId.equals("2024A7PS0255U")) {
				JOptionPane.showMessageDialog(form, "Enter a valid BITS ID.", "Error", JOptionPane.WARNING_MESSAGE);
				return;
			}
			app.lookupStudentDashboard(bitsId);
		});
		form.add(searchBtn);

		panel.add(form, BorderLayout.NORTH);
		return panel;
	}
}
