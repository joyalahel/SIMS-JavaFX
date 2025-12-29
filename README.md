# Student Information Management System (SIMS)

## Project Description
The Student Information Management System (SIMS) is a comprehensive desktop application designed to streamline and manage all core administrative and academic functions within an educational institution. Built using **JavaFX** for a rich, cross-platform graphical user interface and backed by **Microsoft SQL Server** for robust data management, SIMS provides a centralized platform for students, instructors, and administrators.

## Key Features
The system is structured around several key modules to handle various aspects of student and course management:

*   **User Authentication**: Secure login for administrators, instructors, and students.
*   **Student Enrollment**: Management of student records and course registration.
*   **Course Management**: Creation, modification, and scheduling of academic courses.
*   **Attendance Tracking**: Dedicated modules for recording and viewing student and instructor attendance.
*   **Grade Management**: Tools for instructors to input and manage student grades, and for students to view their academic performance.
*   **Administrative Dashboard**: Centralized control panel for system oversight, user management, and reporting.
*   **Reporting & Services**: Includes advanced reporting, data refresh, and backup services.

## Technology Stack
| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Frontend/UI** | JavaFX | Cross-platform desktop application interface. |
| **Backend Logic** | Java (J2SE) | Core application logic and business rules. |
| **Database** | Microsoft SQL Server | Persistent data storage for all system records. |
| **Connectivity** | JDBC | Database connection and query execution. |
| **IDE** | NetBeans | Development environment configuration. |

## Setup and Installation

### 1. Prerequisites
*   Java Development Kit (JDK) 8 or later.
*   Microsoft SQL Server instance.
*   NetBeans IDE (Recommended).

### 2. Database Configuration (Security)
To run the application, you must configure your database credentials securely.

1.  **Create `.env` file**: Copy the provided `.env.example` file to a new file named `.env` in the project root directory.
2.  **Fill Credentials**: Edit the `.env` file with your actual SQL Server connection details.
3.  **Security Note**: The `.env` file is listed in `.gitignore` and will **not** be committed to the repository.

### 3. Running the Project
1.  Open the project in NetBeans IDE.
2.  Ensure all required libraries in the `dist/lib` folder are correctly linked.
3.  Run the `Main.java` file to start the application.
