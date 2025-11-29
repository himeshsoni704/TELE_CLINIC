package core;

/**
 * INTERFACE: IClinicOperations
 * Demonstrates: Interface (contract for dashboards).
 * Defines mandatory methods for dashboards regardless of user type.
 */
public interface IClinicOperations {
    
    /**
     * Updates the UI with the logged-in user's details.
     */
    void updateUserDetails(String fullName, String bitsId, String email, String alerts);

    /**
     * Handles the Emergency Call functionality, triggering logging and notification.
     */
    void handleEmergencyCall();
}