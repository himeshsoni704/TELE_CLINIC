
package core;
public abstract class ClinicUser {

    // --- Encapsulated Fields ---
    private String bitsId;
    private String fullName;
    private String email;
    private String role; // Role is essential for polymorphism/dashboard redirection

    // Constructor
    public ClinicUser(String bitsId, String fullName, String email, String role) {
        this.bitsId = bitsId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // --- Encapsulation: Public Getters/Setters ---

    public String getBitsId() {
        return bitsId;
    }

    public void setBitsId(String bitsId) {
        if (bitsId != null && !bitsId.isEmpty()) {
            this.bitsId = bitsId;
        }
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
    
    public String getRole() {
        return role;
    }

    /**
     * Abstract method forcing subclasses to implement specific details (Polymorphism).
     * @return The specific title for the user's dashboard.
     */
    public abstract String getDashboardTitle();
}