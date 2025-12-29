/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sims.service;
import sims.dao.StudentDao;
import sims.dao.CourseDao;
import sims.dao.EnrollmentDao;
import sims.dao.InstructorDao;
import sims.model.Student;
import sims.model.Course;
import sims.model.Enrollment;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
/**
 *
 * @author Joy Ahel
 */
public class ReportService {
    private StudentDao studentDao;
    private CourseDao courseDao;
    private EnrollmentDao enrollmentDao;
    private InstructorDao instructorDao;

    public ReportService() {
        this.studentDao = new StudentDao();
        this.courseDao = new CourseDao();
        this.enrollmentDao = new EnrollmentDao();
        this.instructorDao = new InstructorDao();
    }
    
 // In ReportService.java - modify the generateStudentReport method:

public Task<Void> generateStudentReport(String studentId) {
    return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            updateMessage("Loading student data...");
            updateProgress(0.1, 1.0);
            Thread.sleep(1000); // 1 second delay
            
            try {
                // Get student data
                Student student = studentDao.getStudentById(studentId);
                List<Enrollment> enrollments = enrollmentDao.getEnrollmentsByStudent(studentId);
                
                updateProgress(0.3, 1.0);
                updateMessage("Processing enrollment data...");
                Thread.sleep(1500); // 1.5 second delay
                
                // Generate filename with timestamp
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "reports/student_report_" + studentId + "_" + timestamp + ".csv";
                
                // Ensure reports directory exists
                new java.io.File("reports").mkdirs();
                
                updateProgress(0.6, 1.0);
                updateMessage("Writing report file...");
                Thread.sleep(1000); // 1 second delay
                
                try (FileWriter writer = new FileWriter(filename)) {
                    // Write header
                    writer.write("Student Report Generated on: " + new Date() + "\n\n");
                    writer.write("Student Information:\n");
                    writer.write("ID,Name,Email,Major\n");
                    writer.write(String.format("%s,%s %s,%s,%s\n\n", 
                        student.getStudentId(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getEmail(),
                        student.getMajor()
                    ));
                    
                    // Write enrollment data
                    writer.write("Course Enrollments:\n");
                    writer.write("Course Code,Course Name,Grade,Letter Grade,Status\n");
                    
                    for (Enrollment enrollment : enrollments) {
                        String letterGrade = getLetterGrade(enrollment.getGrade());
                        writer.write(String.format("%s,%s,%.1f,%s,%s\n",
                            enrollment.getCourseId(),
                            enrollment.getCourseName(),
                            enrollment.getGrade() != null ? enrollment.getGrade() : 0.0,
                            letterGrade,
                            enrollment.getStatus()
                        ));
                    }
                    
                    // Calculate GPA
                    double gpa = calculateGPA(enrollments);
                    writer.write(String.format("\nCumulative GPA: %.2f\n", gpa));
                }
                
                updateProgress(0.9, 1.0);
                updateMessage("Finalizing report...");
                Thread.sleep(500); // 0.5 second delay
                
                updateProgress(1.0, 1.0);
                updateMessage("Report generated successfully!");
                
            } catch (Exception e) {
                updateMessage("Error generating report");
                throw e;
            }
            
            return null;
        }
    };
}
public Task<Void> generateSystemReport() {
    return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            updateMessage("Loading system statistics...");
            updateProgress(0.1, 1.0);
            Thread.sleep(1000);
            
            try {
                // Force reload data to get accurate counts
                updateMessage("Refreshing student data...");
                updateProgress(0.2, 1.0);
                studentDao.loadAllStudents();
                int studentCount = studentDao.getStudentData().size();
                Thread.sleep(500);
                
                updateMessage("Refreshing course data...");
                updateProgress(0.4, 1.0);
                courseDao.loadAllCourses();
                int courseCount = courseDao.getCourseData().size();
                Thread.sleep(500);
                
                updateMessage("Refreshing instructor data...");
                updateProgress(0.6, 1.0);
                instructorDao.loadAllInstructors();
                int instructorCount = instructorDao.getInstructorData().size();
                Thread.sleep(500);
                
                updateProgress(0.8, 1.0);
                updateMessage("Compiling accurate statistics...");
                Thread.sleep(500);
                
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "reports/system_report_" + timestamp + ".csv";
                new java.io.File("reports").mkdirs();
                
                updateProgress(0.9, 1.0);
                updateMessage("Writing accurate report file...");
                Thread.sleep(500);
                
                try (FileWriter writer = new FileWriter(filename)) {
                    writer.write("ACCURATE SYSTEM REPORT\n");
                    writer.write("Generated on: " + new Date() + "\n");
                    writer.write("Data Source: Freshly Loaded from Database\n\n");
                    
                    writer.write("SYSTEM STATISTICS:\n");
                    writer.write("Metric,Count\n");
                    writer.write("Total Students," + studentCount + "\n");
                    writer.write("Total Courses," + courseCount + "\n");
                    writer.write("Total Instructors," + instructorCount + "\n");
                    writer.write("Total Users," + (studentCount + instructorCount + 1) + "\n");
                }
                
                updateProgress(1.0, 1.0);
                updateMessage("Accurate system report generated successfully!");
                Thread.sleep(300);
                
            } catch (Exception e) {
                updateMessage("Error generating system report");
                throw e;
            }
            
            return null;
        }
    };
}
    
    private double calculateGPA(List<Enrollment> enrollments) {
        double totalPoints = 0;
        int totalCredits = 0;
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getGrade() != null) {
                double gradePoints = convertGradeToPoints(enrollment.getGrade());
                // Assuming 3 credits per course - you might want to get actual credits from course
                totalPoints += gradePoints * 3;
                totalCredits += 3;
            }
        }
        
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
    
    private double convertGradeToPoints(double grade) {
        if (grade >= 90) return 4.0;
        else if (grade >= 80) return 3.0;
        else if (grade >= 70) return 2.0;
        else if (grade >= 60) return 1.0;
        else return 0.0;
    }
    
    private String getLetterGrade(Double grade) {
        if (grade == null) return "Not Graded";
        else if (grade >= 97) return "A+";
        else if (grade >= 93) return "A";
        else if (grade >= 90) return "A-";
        else if (grade >= 87) return "B+";
        else if (grade >= 83) return "B";
        else if (grade >= 80) return "B-";
        else if (grade >= 77) return "C+";
        else if (grade >= 73) return "C";
        else if (grade >= 70) return "C-";
        else if (grade >= 67) return "D+";
        else if (grade >= 63) return "D";
        else if (grade >= 60) return "D-";
        else return "F";
    }
}
