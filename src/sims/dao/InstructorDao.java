package sims.dao;

import sims.DatabaseConnection;
import sims.model.Instructor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDao {
    private ObservableList<Instructor> instructorData;
    private UserDao userDao;
    
    public InstructorDao() {
        this.instructorData = FXCollections.observableArrayList();
        this.userDao = new UserDao();
    }
    
    public ObservableList<Instructor> getInstructorData() {
        return instructorData;
    }
    
    public String generateNextInstructorId() {
        String sql = "SELECT instructor_id FROM instructors ORDER BY instructor_id DESC";
        String nextId = "I001";
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString("instructor_id");
                if (lastId != null && lastId.matches("I\\d{3}")) {
                    int number = Integer.parseInt(lastId.substring(1));
                    number++;
                    nextId = String.format("I%03d", number);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next instructor ID: " + e.getMessage());
        }
        return nextId;
    }
    
    public void loadAllInstructors() {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT * FROM instructors ORDER BY instructor_id";
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Instructor instructor = new Instructor();
                instructor.setInstructorId(rs.getString("instructor_id"));
                instructor.setFirstName(rs.getString("first_name"));
                instructor.setLastName(rs.getString("last_name"));
                instructor.setEmail(rs.getString("email"));
                instructor.setDepartment(rs.getString("department"));
                instructor.setHireDate(rs.getDate("hire_date"));
                instructors.add(instructor);
            }
        } catch (SQLException e) {
            System.err.println("Error loading instructors: " + e.getMessage());
        }
        
        Platform.runLater(() -> {
            instructorData.clear();
            instructorData.addAll(instructors);
        });
    }
    
    public boolean insertInstructor(Instructor instructor, String password) {
        boolean instructorInserted = insertInstructorRecord(instructor);
        if (!instructorInserted) {
            return false;
        }
        
        boolean userCreated = createInstructorUserAccount(instructor, password);
        if (!userCreated) {
            System.err.println("Warning: Instructor record created but user account creation failed for: " + instructor.getEmail());
        }
        
        return true;
    }
    
    private boolean insertInstructorRecord(Instructor instructor) {
        String sql = "INSERT INTO instructors (instructor_id, first_name, last_name, email, department, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, instructor.getInstructorId());
            ps.setString(2, instructor.getFirstName());
            ps.setString(3, instructor.getLastName());
            ps.setString(4, instructor.getEmail());
            ps.setString(5, instructor.getDepartment());
            ps.setDate(6, instructor.getHireDate());
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                Platform.runLater(() -> instructorData.add(instructor));
            }
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error inserting instructor record: " + e.getMessage());
            return false;
        }
    }
    
    private boolean createInstructorUserAccount(Instructor instructor, String password) {
        try {
            String username = instructor.getInstructorId().toLowerCase();
            String fullName = instructor.getFirstName() + " " + instructor.getLastName();
            
            String sql = "INSERT INTO users (username, password, role, full_name, email) VALUES (?, ?, 'instructor', ?, ?)";
            
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // REVERT: Store plain text password
                ps.setString(1, username);
                ps.setString(2, password); // Plain text
                ps.setString(3, fullName);
                ps.setString(4, instructor.getEmail());
                
                int rowsAffected = ps.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating instructor user account: " + e.getMessage());
        }
        return false;
    } 
    public boolean updateInstructor(Instructor instructor, String newPassword) {
        String sql = "UPDATE instructors SET first_name = ?, last_name = ?, email = ?, department = ?, hire_date = ? WHERE instructor_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, instructor.getFirstName());
            ps.setString(2, instructor.getLastName());
            ps.setString(3, instructor.getEmail());
            ps.setString(4, instructor.getDepartment());
            ps.setDate(5, instructor.getHireDate());
            ps.setString(6, instructor.getInstructorId());
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    updateInstructorUserAccount(instructor, newPassword);
                }
                
                Platform.runLater(() -> {
                    for (int i = 0; i < instructorData.size(); i++) {
                        if (instructorData.get(i).getInstructorId().equals(instructor.getInstructorId())) {
                            instructorData.set(i, instructor);
                            break;
                        }
                    }
                });
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating instructor: " + e.getMessage());
            return false;
        }
    }
    
     private void updateInstructorUserAccount(Instructor instructor, String newPassword) {
        try {
            String username = instructor.getInstructorId().toLowerCase();
            String fullName = instructor.getFirstName() + " " + instructor.getLastName();
            
            String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, email = ? WHERE email = ?";
            
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // REVERT: Store plain text password
                ps.setString(1, username);
                ps.setString(2, newPassword); // Plain text
                ps.setString(3, fullName);
                ps.setString(4, instructor.getEmail());
                ps.setString(5, instructor.getEmail());
                
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error updating instructor user account: " + e.getMessage());
        }
    }

    
    public boolean deleteInstructor(String instructorId) {
        String instructorEmail = getInstructorEmail(instructorId);
        
        boolean instructorDeleted = deleteInstructorRecord(instructorId);
        if (!instructorDeleted) {
            return false;
        }
        
        if (instructorEmail != null) {
            deleteInstructorUserAccount(instructorEmail);
        }
        
        return true;
    }
    
    private boolean deleteInstructorRecord(String instructorId) {
        String sql = "DELETE FROM instructors WHERE instructor_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, instructorId);
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                Platform.runLater(() -> 
                    instructorData.removeIf(instructor -> instructor.getInstructorId().equals(instructorId))
                );
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting instructor record: " + e.getMessage());
            return false;
        }
    }
    
    private String getInstructorEmail(String instructorId) {
        String sql = "SELECT email FROM instructors WHERE instructor_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, instructorId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting instructor email: " + e.getMessage());
        }
        return null;
    }
    
    private void deleteInstructorUserAccount(String instructorEmail) {
        try {
            String sql = "DELETE FROM users WHERE email = ? AND role = 'instructor'";
            
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, instructorEmail);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error deleting instructor user account: " + e.getMessage());
        }
    }
}