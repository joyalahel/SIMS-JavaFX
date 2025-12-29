package sims.dao;

import sims.DatabaseConnection;
import sims.model.Course;
import sims.model.Instructor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private ObservableList<Course> courseData;
    
    public CourseDao() {
        this.courseData = FXCollections.observableArrayList();
    }
    
    public ObservableList<Course> getCourseData() {
        return courseData;
    }
    
    // ADD THIS METHOD - Required for AdminAttendanceController
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = """
            SELECT c.*, i.first_name + ' ' + i.last_name as instructor_name 
            FROM courses c 
            LEFT JOIN instructors i ON c.instructor_id = i.instructor_id 
            ORDER BY c.course_code
            """;
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setCourseCode(rs.getString("course_code"));
                course.setInstructorId(rs.getString("instructor_id"));
                course.setInstructorName(rs.getString("instructor_name"));
                course.setCredits(rs.getInt("credits"));
                course.setDepartment(rs.getString("department"));
                course.setDescription(rs.getString("description"));
                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Error loading all courses: " + e.getMessage());
        }
        return courses;
    }
    
    public String generateNextCourseId() {
        String sql = "SELECT course_id FROM courses ORDER BY course_id DESC";
        String nextId = "C001";
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString("course_id");
                if (lastId != null && lastId.matches("C\\d{3}")) {
                    int number = Integer.parseInt(lastId.substring(1));
                    number++;
                    nextId = String.format("C%03d", number);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next course ID: " + e.getMessage());
        }
        return nextId;
    }
    
    public void loadAllCourses() {
        List<Course> courses = getAllCourses(); // Reuse the method above
        
        // Update ObservableList on JavaFX Application Thread
        Platform.runLater(() -> {
            courseData.clear();
            courseData.addAll(courses);
        });
    }
    
    public ObservableList<Course> getCoursesByInstructor(String instructorId) {
        ObservableList<Course> instructorCourses = FXCollections.observableArrayList();
        String sql = """
            SELECT c.*, i.first_name + ' ' + i.last_name as instructor_name 
            FROM courses c 
            LEFT JOIN instructors i ON c.instructor_id = i.instructor_id 
            WHERE c.instructor_id = ?
            ORDER BY c.course_code
            """;
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, instructorId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setCourseId(rs.getString("course_id"));
                    course.setCourseName(rs.getString("course_name"));
                    course.setCourseCode(rs.getString("course_code"));
                    course.setInstructorId(rs.getString("instructor_id"));
                    course.setInstructorName(rs.getString("instructor_name"));
                    course.setCredits(rs.getInt("credits"));
                    course.setDepartment(rs.getString("department"));
                    course.setDescription(rs.getString("description"));
                    instructorCourses.add(course);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading courses by instructor: " + e.getMessage());
        }
        return instructorCourses;
    }
    
    public List<Instructor> getAllInstructors() {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT instructor_id, first_name, last_name FROM instructors ORDER BY first_name, last_name";
        
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Instructor instructor = new Instructor();
                instructor.setInstructorId(rs.getString("instructor_id"));
                instructor.setFirstName(rs.getString("first_name"));
                instructor.setLastName(rs.getString("last_name"));
                instructors.add(instructor);
            }
        } catch (SQLException e) {
            System.err.println("Error loading instructors: " + e.getMessage());
        }
        return instructors;
    }
    
    public boolean insertCourse(Course course) {
        String sql = "INSERT INTO courses (course_id, course_name, course_code, instructor_id, credits, department, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, course.getCourseId());
            ps.setString(2, course.getCourseName());
            ps.setString(3, course.getCourseCode());
            ps.setString(4, course.getInstructorId());
            ps.setInt(5, course.getCredits());
            ps.setString(6, course.getDepartment());
            ps.setString(7, course.getDescription());
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                // Add to observable list on JavaFX Application Thread
                Platform.runLater(() -> courseData.add(course));
            }
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error inserting course: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET course_name = ?, course_code = ?, instructor_id = ?, credits = ?, department = ?, description = ? WHERE course_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, course.getCourseName());
            ps.setString(2, course.getCourseCode());
            ps.setString(3, course.getInstructorId());
            ps.setInt(4, course.getCredits());
            ps.setString(5, course.getDepartment());
            ps.setString(6, course.getDescription());
            ps.setString(7, course.getCourseId());
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                // Update observable list on JavaFX Application Thread
                Platform.runLater(() -> {
                    for (int i = 0; i < courseData.size(); i++) {
                        if (courseData.get(i).getCourseId().equals(course.getCourseId())) {
                            courseData.set(i, course);
                            break;
                        }
                    }
                });
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteCourse(String courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, courseId);
            
            int rowsAffected = ps.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                // Remove from observable list on JavaFX Application Thread
                Platform.runLater(() -> 
                    courseData.removeIf(course -> course.getCourseId().equals(courseId))
                );
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            return false;
        }
    }
}