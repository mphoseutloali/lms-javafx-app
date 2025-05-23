# Learning Management System (LMS)

A desktop-based Learning Management System built with JavaFX and PostgreSQL, featuring a modern and intuitive user interface with educational-themed styling.

![LMS Dashboard]

- *User Authentication*
  - Secure admin login system
  - Session management
  - Password protection

- *Student Management*
  - Add, edit, and delete student records
  - Search and filter functionality
  - Pagination for large datasets

- *Course Management*
  - Create and manage course offerings
  - Course descriptions and credit hours
  - Search and filter functionality

- *Enrollment Tracking*
  - Enroll students in courses
  - Track progress with visual indicators
  - Filter by completion status
  - Export progress data to CSV

- *Dashboard Analytics*
  - Overview of system statistics
  - Visual charts for enrollment distribution
  - Progress tracking across courses
  - Recent activity monitoring

- *Modern UI*
  - styling
  - Responsive layout
  - Visual progress indicators
  - Intuitive navigation

## 🛠 Technologies

- *Java 17* - Core programming language
- *JavaFX* - UI framework
- *PostgreSQL* - Database
- *JDBC* - Database connectivity
- *Maven* - Dependency management and build tool
- *CSS* - Custom styling

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

### Setup Steps

1. *Clone the repository*
   git clone [https://github.com//lms-system.git](https://github.com/mphoseutloali/lms-javafx-app)
   cd lms-system

2. *Configure the database*
   - Create a PostgreSQL database named lms_db
   - Update database connection settings in `src/main/java/com/example/demo/util/DatabaseConnection.java
     private static final String URL = "jdbc:postgresql://localhost:5432/lms_db";
     private static final String USER = "postgres";
     private static final String PASSWORD = "123456";


3. *Initialize the database*
   - Run the SQL scripts in the src/main/resources/sql directory:
     psql -U postgres -d lms_db -f src/main/resources/sql/schema.sql
     psql -U postgres -d lms_db -f src/main/resources/sql/admin_table.sql

4. *Build the project*
   mvn clean package

5. *Run the application*
   mvn javafx:run
   
## 🗄 Database Schema

The system uses the following database tables:

- *students* - Stores student information
- *courses* - Stores course information
- *enrollments* - Tracks student enrollments in courses
- *admin_users* - Stores administrator credentials

## 🔐 Login Credentials

Default admin login:
- *Username*: Mpho
- *Password*: mpho123

### Login Screen
Enter your admin credentials to access the system.

### Dashboard
The dashboard provides an overview of system statistics and recent activity.

### Students
Manage student records:
- View all students
- Add new students
- Edit existing student information
- Delete students

### Courses
Manage course offerings:
- View all courses
- Add new courses
- Edit course details
- Delete courses

### Enrollments
Track student enrollments:
- View all enrollments
- Add new enrollments
- Update progress
- Filter by completion status
- Export progress data to CSV

## 📁 Project Structure

src/main/java/com/example/demo/
├── Main.java                  # Application entry point
├── controller/                # UI controllers
│   ├── LoginController.java
│   ├── MainController.java
│   ├── DashboardController.java
│   ├── StudentsController.java
│   ├── CoursesController.java
│   └── EnrollmentsController.java
├── dao/                       # Data Access Objects
│   ├── AdminDAO.java
│   ├── StudentDAO.java
│   ├── CourseDAO.java
│   └── EnrollmentDAO.java
├── model/                     # Data models
│   ├── Admin.java
│   ├── Student.java
│   ├── Course.java
│   └── Enrollment.java
└── util/                      # Utility classes
    ├── DatabaseConnection.java
    ├── SessionManager.java
    |__ CsvExporter.java

src/main/resources/
├── fxml/                      # UI layouts
│   ├── LoginView.fxml
│   ├── MainView.fxml
│   ├── DashboardView.fxml
│   ├── StudentsView.fxml
│   ├── CoursesView.fxml
│   └── EnrollmentsView.fxml
└── sql/                       # SQL scripts
    └── admin_table.sql

## 🎨 UI Customization

The application uses a custom CSS theme located at src/main/resources/css/lms-theme.css. You can modify this file to change the appearance of the application.

Key style classes:
- .dashboard-card - Dashboard statistic cards
- .icon-book, .icon-pen, etc. - Educational icons
- .progress-bar-low, .progress-bar-medium, .progress-bar-high - Progress indicators
- .login-container - Login screen styling

## 📊 Data Export

The system allows exporting enrollment progress data to CSV format. The export functionality is implemented in the CsvExporter utility class.

## 🔄 Session Management

User sessions are managed by the SessionManager utility class, which keeps track of the currently logged-in admin user.

## 👥 Contributors

- Your Name - Initial work and development

## 🙏 Acknowledgments

- JavaFX community for the UI framework
- PostgreSQL team for the database engine
- All open-source libraries used in this project
