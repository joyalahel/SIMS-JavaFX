package sims.dao;

import sims.DatabaseConnection;
import sims.model.Student;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {
    private ObservableList<Student> studentData;
    
    public StudentDao() {
        this.studentData = FXCollections.observableArrayList();
    }
    
    public ObservableList<Student> getStudentData() {
        return studentData;
    }
    
    public String generateNextStudentId() {
        String sql = "SELECT student_id FROM students ORDER BY student_id DESC";
        String nextId = "A001";
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString("student_id");
                if (lastId != null && lastId.matches("A\\d{3}")) {
                    int number = Integer.parseInt(lastId.substring(1));
                    number++;
                    nextId = String.format("A%03d", number);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next student ID: " + e.getMessage());
            e.printStackTrace();
        }
        return nextId;
    }
    
    public void loadAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("student_id"));
                student.setFirstName(rs.getString("first_name"));
                student.setLastName(rs.getString("last_name"));
                student.setEmail(rs.getString("email"));
                student.setDateOfBirth(rs.getDate("date_of_birth"));
                student.setMajor(rs.getString("major"));
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Update ObservableList on JavaFX Application Thread
        Platform.runLater(() -> {
            studentData.clear();
            studentData.addAll(students);
        });
    }
    
    public boolean insertStudent(Student student, String password) {
        // First insert into students table
        boolean studentInserted = insertStudentRecord(student);
        if (!studentInserted) {
            return false;
        }
        
        // Then create user account with provided password
        boolean userCreated = createStudentUserAccount(student, password);
        
        if (!userCreated) {
            System.err.println("Warning: Student record created but user account creation failed for: " + student.getEmail());
        }
        
        return true;
    }
    
    private boolean insertStudentRecord(Student student) {
        String sql = "INSERT INTO students (student_id, first_name, last_name, email, date_of_birth, major) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setDate(5, student.getDateOfBirth());
            ps.setString(6, student.getMajor());
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                // Add to observable list on JavaFX Application Thread
                Platform.runLater(() -> studentData.add(student));
            }
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error inserting student record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
  private boolean createStudentUserAccount(Student student, String password) {
        try {
            String username = student.getStudentId().toLowerCase();
            String fullName = student.getFirstName() + " " + student.getLastName();
            
            String sql = "INSERT INTO users (username, password, role, full_name, email) VALUES (?, ?, 'student', ?, ?)";
            
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // REVERT: Store plain text password (no hashing)
                ps.setString(1, username);
                ps.setString(2, password); // Plain text
                ps.setString(3, fullName);
                ps.setString(4, student.getEmail());
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ User account created for student: " + username);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating student user account: " + e.getMessage());
        }
        return false;
    }
    
    public boolean updateStudent(Student student, String newPassword) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, date_of_birth = ?, major = ? WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setString(3, student.getEmail());
            ps.setDate(4, student.getDateOfBirth());
            ps.setString(5, student.getMajor());
            ps.setString(6, student.getStudentId());
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                // Update user account with new password if provided
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    updateStudentUserAccount(student, newPassword);
                } else {
                    // Just update user info without changing password
                    updateStudentUserAccount(student, null);
                }
                
                // Update observable list on JavaFX Application Thread
                Platform.runLater(() -> {
                    for (int i = 0; i < studentData.size(); i++) {
                        if (studentData.get(i).getStudentId().equals(student.getStudentId())) {
                            studentData.set(i, student);
                            break;
                        }
                    }
                });
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
  private void updateStudentUserAccount(Student student, String newPassword) {
        try {
            String username = student.getStudentId().toLowerCase();
            String fullName = student.getFirstName() + " " + student.getLastName();
            
            String sql;
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                // REVERT: Store plain text password
                sql = "UPDATE users SET username = ?, password = ?, full_name = ?, email = ? WHERE email = ? OR username = ?";
            } else {
                sql = "UPDATE users SET username = ?, full_name = ?, email = ? WHERE email = ? OR username = ?";
            }
            
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    ps.setString(1, username);
                    ps.setString(2, newPassword); // Plain text
                    ps.setString(3, fullName);
                    ps.setString(4, student.getEmail());
                    ps.setString(5, student.getEmail());
                    ps.setString(6, student.getStudentId().toLowerCase());
                } else {
                    ps.setString(1, username);
                    ps.setString(2, fullName);
                    ps.setString(3, student.getEmail());
                    ps.setString(4, student.getEmail());
                    ps.setString(5, student.getStudentId().toLowerCase());
                }
                
                ps.executeUpdate();
                System.out.println("User account updated for student: " + username);
            }
        } catch (SQLException e) {
            System.err.println("Error updating student user account: " + e.getMessage());
        }
    }

    
    public boolean deleteStudent(String studentId) {
        // First get student email before deletion
        String studentEmail = getStudentEmail(studentId);
        
        // Delete from students table
        boolean studentDeleted = deleteStudentRecord(studentId);
        if (!studentDeleted) {
            return false;
        }
        
        // Delete user account if student was found and deleted
        if (studentEmail != null) {
            deleteStudentUserAccount(studentEmail);
        }
        
        return true;
    }
    
    private boolean deleteStudentRecord(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, studentId);
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                // Remove from observable list on JavaFX Application Thread
                Platform.runLater(() -> 
                    studentData.removeIf(student -> student.getStudentId().equals(studentId))
                );
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting student record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private String getStudentEmail(String studentId) {
        String sql = "SELECT email FROM students WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, studentId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student email: " + e.getMessage());
        }
        return null;
    }
    
    private void deleteStudentUserAccount(String studentEmail) {
        try {
            String sql = "DELETE FROM users WHERE email = ? AND role = 'student'";
            
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, studentEmail);
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("User account deleted for email: " + studentEmail);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting student user account: " + e.getMessage());
        }
    }
    
    // Method to get current password for a student (for editing)
    public String getStudentPassword(String studentId) {
        String sql = "SELECT u.password FROM users u INNER JOIN students s ON u.email = s.email WHERE s.student_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, studentId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student password: " + e.getMessage());
        }
        return null;
    }
    
    public Student getStudentById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, studentId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getString("student_id"));
                    student.setFirstName(rs.getString("first_name"));
                    student.setLastName(rs.getString("last_name"));
                    student.setEmail(rs.getString("email"));
                    student.setDateOfBirth(rs.getDate("date_of_birth"));
                    student.setMajor(rs.getString("major"));
                    return student;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}