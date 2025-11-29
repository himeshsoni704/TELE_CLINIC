===== TELE_CLINIC APPLICATION =====

REQUIREMENTS:
- Java 8 or higher
- Maven 3.9.11 or higher
- PowerShell (pwsh)

===== RUNNING BACKEND (Telegram Bot) =====

1. Navigate to backend directory and compile with Maven:
cd 'C:\Users\Kusha\OneDrive\Desktop\tele_clinic\backend\oops'
& 'C:\Program Files\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd' --% clean compile

2. Run the Telegram bot:
& 'C:\Program Files\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd' --% exec:java -Dexec.mainClass="bot.BotRunner"

Note: The bot will watch for messages in backend/oops/messages.txt and emergency_logs.txt, 
then send notifications to Telegram using data from frontend/medical_registrations.csv

===== RUNNING FRONTEND (GUI Application) =====

1. Navigate to frontend directory:
cd 'C:\Users\Kusha\OneDrive\Desktop\tele_clinic\frontend'

2. Compile the frontend:
javac -d bin app\*.java core\*.java util\*.java

3. Run the frontend application:
java -cp bin app.BPDCClinicApp

The frontend will open a GUI with:
- Login screen
- Medical registration form
- Student dashboard
- Admin dashboard

===== FILE PATHS =====

The application uses relative file paths from their respective directories:

Backend (running from backend/oops):
- messages.txt (in backend/oops)
- emergency_logs.txt (relative to frontend)
- medical_registrations.csv (relative to frontend)

Frontend (running from frontend):
- credentials.csv
- medical_registrations.csv
- prescriptions.txt
- emergency_logs.txt
- image_1.png (in app folder)

===== TYPICAL WORKFLOW =====

1. Open PowerShell window 1 - Run BACKEND:
   cd 'C:\Users\Kusha\OneDrive\Desktop\tele_clinic\backend\oops'
   & 'C:\Program Files\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd' --% clean compile
   & 'C:\Program Files\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd' --% exec:java -Dexec.mainClass="bot.BotRunner"

2. Open PowerShell window 2 - Run FRONTEND:
   cd 'C:\Users\Kusha\OneDrive\Desktop\tele_clinic\frontend'
   javac -d bin app\*.java core\*.java util\*.java
   java -cp bin app.BPDCClinicApp

3. Use the frontend GUI to:
   - Register medical information (saves to medical_registrations.csv)
   - View prescriptions
   - Trigger emergency alerts (saves to emergency_logs.txt)

4. The backend bot will:
   - Monitor log files for new entries
   - Send Telegram notifications to guardians
   - Use the medical data to map student IDs to guardian IDs