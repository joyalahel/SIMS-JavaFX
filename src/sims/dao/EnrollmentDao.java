package sims.dao;

import sims.DatabaseConnection;
import sims.model.Enrollment;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {
    private ObservableList<Enrollment> enrollmentData;
    
    public EnrollmentDao() {
        this.enrollmentData = FXCollections.observableArrayList();
    }
    
    public ObservableList<Enrollment> getEnrollmentData() {
        return enrollmentData;
    }
    
    public void loadAllEnrollments() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, e.student_id, e.course_id, e.grade, e.status,
                   s.first_name + ' ' + s.last_name as student_name,
                   c.course_name
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            JOIN courses c ON e.course_id = c.course_id
            ORDER BY c.course_name, s.first_name
            """;
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getString("student_id"));
                enrollment.setStudentName(rs.getString("student_name"));
                enrollment.setCourseId(rs.getString("course_id"));
                enrollment.setCourseName(rs.getString("course_name"));
                enrollment.setGrade(rs.getDouble("grade"));
                if (rs.wasNull()) enrollment.setGrade(null);
                enrollment.setStatus(rs.getString("status"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            System.err.println("Error loading enrollments: " + e.getMessage());
        }
        
        Platform.runLater(() -> {
            enrollmentData.clear();
            enrollmentData.addAll(enrollments);
        });
    }
    
   public List<Enrollment> getEnrollmentsByInstructor(String instructorId) {
    List<Enrollment> enrollments = new ArrayList<>();
    String sql = """
        SELECT e.enrollment_id, e.student_id, e.course_id, e.grade, e.status,
               s.first_name + ' ' + s.last_name as student_name,
               c.course_name, c.instructor_id
        FROM enrollments e
        JOIN students s ON e.student_id = s.student_id
        JOIN courses c ON e.course_id = c.course_id
        WHERE c.instructor_id = ?
        ORDER BY c.course_name, s.first_name
        """;
    
    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, instructorId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getString("student_id"));
                enrollment.setStudentName(rs.getString("student_name"));
                enrollment.setCourseId(rs.getString("course_id"));
                enrollment.setCourseName(rs.getString("course_name"));
                enrollment.setGrade(rs.getDouble("grade"));
                if (rs.wasNull()) enrollment.setGrade(null);
                enrollment.setStatus(rs.getString("status"));
                enrollments.add(enrollment);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error loading enrollments by instructor: " + e.getMessage());
    }
    return enrollments;
}// In EnrollmentDao.java - modify the getEnrollmentsByStudent method
public List<Enrollment> getEnrollmentsByStudent(String studentId) {
    List<Enrollment> enrollments = new ArrayList<>();
    String sql = """
        SELECT e.enrollment_id, e.student_id, e.course_id, e.grade, e.status,
               s.first_name + ' ' + s.last_name as student_name,
               c.course_name, c.course_code, c.credits
        FROM enrollments e
        JOIN students s ON e.student_id = s.student_id
        JOIN courses c ON e.course_id = c.course_id
        WHERE e.student_id = ?
        ORDER BY c.course_name
        """;
    
    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, studentId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getString("student_id"));
                enrollment.setStudentName(rs.getString("student_name"));
                enrollment.setCourseId(rs.getString("course_id"));
                enrollment.setCourseName(rs.getString("course_name"));
                enrollment.setCredits(rs.getInt("credits")); // Add this line
                
                // Handle grade properly
                double grade = rs.getDouble("grade");
                if (rs.wasNull()) {
                    enrollment.setGrade(null);
                } else {
                    enrollment.setGrade(grade);
                }
                
                enrollment.setStatus(rs.getString("status"));
                enrollments.add(enrollment);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error loading enrollments by student: " + e.getMessage());
        e.printStackTrace();
    }
    
    return enrollments;
}

    public List<Enrollment> getEnrollmentsByCourse(String courseId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, e.student_id, e.course_id, e.grade, e.status,
                   s.first_name + ' ' + s.last_name as student_name,
                   c.course_name
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            JOIN courses c ON e.course_id = c.course_id
            WHERE e.course_id = ?
            ORDER BY s.first_name
            """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getString("student_id"));
                    enrollment.setStudentName(rs.getString("student_name"));
                    enrollment.setCourseId(rs.getString("course_id"));
                    enrollment.setCourseName(rs.getString("course_name"));
                    enrollment.setGrade(rs.getDouble("grade"));
                    if (rs.wasNull()) enrollment.setGrade(null);
                    enrollment.setStatus(rs.getString("status"));
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading enrollments by course: " + e.getMessage());
        }
        return enrollments;
    }
    
public boolean updateGrade(int enrollmentId, Double grade) {
    String sql = "UPDATE enrollments SET grade = ? WHERE enrollment_id = ?";
    
    try (Connection conn = DatabaseConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        if (grade == null) {
            ps.setNull(1, java.sql.Types.DECIMAL);
        } else {
            ps.setDouble(1, grade);
        }
        ps.setInt(2, enrollmentId);
        
        int rowsAffected = ps.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating grade: " + e.getMessage());
        return false;
    }
}
    public boolean enrollStudent(String studentId, String courseId) {
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, studentId);
            ps.setString(2, courseId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error enrolling student: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isStudentEnrolled(String studentId, String courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, studentId);
            ps.setString(2, courseId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking enrollment: " + e.getMessage());
        }
        return false;
    }
}