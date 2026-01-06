# Tele-Clinic App

**Tele-Clinic** is an integrated emergency medical response and clinic management system designed specifically for the **BITS Pilani Dubai Campus (BPDC)**.  
It streamlines communication between students and medical staff, reducing emergency response times from hours to **under 15 minutes**.

---

## 🚀 Key Features

- **Real-Time Emergency Alerts:** One-click emergency button that sends instant notifications to clinic staff and campus authorities.  
- **Telegram Bot Integration:** Backend bot shares student geolocation with nurses and sends automated alerts to guardians.  
- **Centralized Medical Dashboard:** Securely stores and displays student medical records, including allergies, insurance status, and health history.  
- **Automated Workflows:** Streamlines coordination between nurses, wardens, and transport services to eliminate manual bottlenecks.  
- **Role-Based Access:** Dedicated interfaces for Students (registration, dashboard, alerts) and Admins/Nurses (record management, prescriptions, monitoring).

---

## 🛠️ Technology Stack

- **Language:** Java 8+  
- **GUI Framework:** Java Swing & AWT  
- **Backend:** Telegram Bot API via Maven dependencies  
- **Monitoring:** Java WatchService and RandomAccessFile for real-time log detection  
- **Build Tool:** Maven 3.9+  
- **Storage:** File-based (CSV and TXT)  

---

## 📁 Project Structure

The project follows a **modular architecture** to separate concerns:

- **app Package:** Manages the GUI flow and main application manager (`BPDCClinicApp.java`)  
- **core Package:** Contains domain models (`ClinicUser`, `Student`, `Admin`) and business logic  
- **util Package:** Handles infrastructure tasks like file I/O operations and CSV parsing  
- **backend:** Independent process for the Telegram Bot (`AutoReplyBot.java`, `Notifier.java`)  

---

## ⚙️ Installation & Usage

**Clone the repository:**
```bash
git clone https://github.com/himeshsoni704/TELE_CLINIC
